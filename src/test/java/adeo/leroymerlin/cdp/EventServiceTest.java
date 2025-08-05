package adeo.leroymerlin.cdp;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventService eventService; // Injects the real service

    @Test
    void updateEvent_ShouldUpdateStarsAndComment_WhenEventExists() {
        // ARRANGE - Prepare an existing event
        Long eventId = 1L;
        Event existingEvent = new Event();
        existingEvent.setId(eventId);
        existingEvent.setNbStars(3);
        existingEvent.setComment("Old comment");
        existingEvent.setTitle("Event Title"); 

        // Update data coming from the frontend
        Event updateData = new Event();
        updateData.setNbStars(5);
        updateData.setComment("Excellent event!");

        // Mock the repository
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(existingEvent));
        when(eventRepository.save(any(Event.class))).thenReturn(existingEvent);

        // ACT - Call the real service method
        eventService.updateEvent(eventId, updateData);

        // ASSERT - Verify the behavior
        // 1. Check that findById was called with the correct ID
        verify(eventRepository).findById(eventId);

        // 2. Check that save was called once
        verify(eventRepository).save(existingEvent);

        // 3. Check that the right values were updated
        assertEquals(5, existingEvent.getNbStars(),
                "Stars should be updated");
        assertEquals("Excellent event!", existingEvent.getComment(),
                "Comment should be updated");
        assertEquals("Event Title", existingEvent.getTitle(),
                "Title should NOT be changed");
    }

    @Test
    void updateEvent_ShouldHandleNullValues() {
        // ARRANGE
        Long eventId = 1L;
        Event existingEvent = new Event();
        existingEvent.setId(eventId);
        existingEvent.setNbStars(3);
        existingEvent.setComment("Old comment");

        Event updateWithNulls = new Event();
        updateWithNulls.setNbStars(null); // Null value
        updateWithNulls.setComment(null); // Null value

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(existingEvent));

        eventService.updateEvent(eventId, updateWithNulls);

        // ASSERT - Values should not change if input is null
        assertEquals(3, existingEvent.getNbStars());
        assertEquals("Old comment", existingEvent.getComment());
        verify(eventRepository).save(existingEvent);
    }
}
