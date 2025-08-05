package adeo.leroymerlin.cdp;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

    @Test
void getFilteredEvents_ShouldReturnMatchingEvents_WhenMemberNameMatches() {
    // ARRANGE - Create test data
    Member memberWalsh = new Member();
    memberWalsh.setName("Queen Anika Walsh");
    
    Member memberOther = new Member();
    memberOther.setName("John Doe");
    
    Band metallica = new Band();
    metallica.setName("Metallica");
    metallica.setMembers(Set.of(memberWalsh));
    
    Band otherBand = new Band();
    otherBand.setName("Other Band");
    otherBand.setMembers(Set.of(memberOther));
    
    Event eventWithWalsh = new Event();
    eventWithWalsh.setTitle("GrasPop Metal Meeting");
    eventWithWalsh.setBands(Set.of(metallica));
    
    Event eventWithoutWalsh = new Event();
    eventWithoutWalsh.setTitle("Other Event");
    eventWithoutWalsh.setBands(Set.of(otherBand));
    
    List<Event> allEvents = Arrays.asList(eventWithWalsh, eventWithoutWalsh);
    
    // Mock the repository
    when(eventRepository.findAll()).thenReturn(allEvents);
    
    // ACT - Call the filtering function
    List<Event> result = eventService.getFilteredEvents("Wa");
    
    // ASSERT - Check the results
    assertEquals(1, result.size(), "Should return exactly 1 event");
    assertEquals("GrasPop Metal Meeting", result.get(0).getTitle(),
                "Should return the event with Walsh");
}

@Test
void getFilteredEvents_ShouldReturnEmpty_WhenNoMemberMatches() {
    // ARRANGE - Create test data
    Member member = new Member();
    member.setName("John Doe");
    
    Band band = new Band();
    band.setMembers(Set.of(member));
    
    Event event = new Event();
    event.setTitle("Test Event");
    event.setBands(Set.of(band));
    
    when(eventRepository.findAll()).thenReturn(List.of(event));
    
    // ACT - Call the filtering function
    List<Event> result = eventService.getFilteredEvents("xyz");
    
    // ASSERT - Should return no results
    assertEquals(0, result.size(), "Should return no event");
}
}
