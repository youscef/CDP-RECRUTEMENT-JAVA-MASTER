package adeo.leroymerlin.cdp;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
        assertEquals("GrasPop Metal Meeting [1]", result.get(0).getTitle(),
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

    @Test
    void getFilteredEvents_ShouldAddCountsToEventAndBands() {
        // ARRANGE - Create data with specific counts
        Member member1 = new Member();
        member1.setName("Queen Anika Walsh");

        Member member2 = new Member();
        member2.setName("John Doe");

        Member member3 = new Member();
        member3.setName("Jane Smith");

        // Band with 2 members
        Band metallica = new Band();
        metallica.setName("Metallica");
        metallica.setMembers(Set.of(member1, member2));

        // Band with 1 member
        Band pinkFloyd = new Band();
        pinkFloyd.setName("Pink Floyd");
        pinkFloyd.setMembers(Set.of(member3));

        // Event with 2 bands
        Event event = new Event();
        event.setTitle("GrasPop Metal Meeting");
        event.setBands(Set.of(metallica, pinkFloyd));

        List<Event> allEvents = List.of(event);
        when(eventRepository.findAll()).thenReturn(allEvents);

        // ACT - Call the search
        List<Event> result = eventService.getFilteredEvents("Walsh");

        // ASSERT - Check the counts
        assertEquals(1, result.size(), "Should return 1 event");

        Event resultEvent = result.get(0);
        assertEquals("GrasPop Metal Meeting [2]", resultEvent.getTitle(),
                "The title should include the band count [2]");

        assertEquals(2, resultEvent.getBands().size(),
                "The event should have 2 bands");

        // Check each band's member count
        boolean metallicaFound = false;
        boolean pinkFloydFound = false;

        for (Band band : resultEvent.getBands()) {
            if (band.getName().startsWith("Metallica")) {
                assertEquals("Metallica [2]", band.getName(),
                        "Metallica should have [2] members");
                metallicaFound = true;
            } else if (band.getName().startsWith("Pink Floyd")) {
                assertEquals("Pink Floyd [1]", band.getName(),
                        "Pink Floyd should have [1] member");
                pinkFloydFound = true;
            }
        }

        assertTrue(metallicaFound, "Metallica should be found");
        assertTrue(pinkFloydFound, "Pink Floyd should be found");
    }

    @Test
    void getFilteredEvents_ShouldHandleEmptyBands() {
        // ARRANGE - Event without bands
        Event eventWithoutBands = new Event();
        eventWithoutBands.setTitle("Empty Event");
        eventWithoutBands.setBands(null);

        List<Event> allEvents = List.of(eventWithoutBands);
        when(eventRepository.findAll()).thenReturn(allEvents);

        // ACT
        List<Event> result = eventService.getFilteredEvents("anything");
        

        // ASSERT - No event should match since there are no members
        assertEquals(0, result.size(),
                "No event should match without bands");
    }

    @Test
    void getFilteredEvents_ShouldHandleBandWithNoMembers() {
        // ARRANGE - Band without members
        Band emptyBand = new Band();
        emptyBand.setName("Empty Band");
        emptyBand.setMembers(null);

        Event event = new Event();
        event.setTitle("Event with Empty Band");
        event.setBands(Set.of(emptyBand));

        List<Event> allEvents = List.of(event);
        when(eventRepository.findAll()).thenReturn(allEvents);

        // ACT
        List<Event> result = eventService.getFilteredEvents("anything");

        // ASSERT - No match since there are no members
        assertEquals(0, result.size(),
                "No event should match without members");
    }

    @Test
    void getFilteredEvents_ShouldNotModifyOriginalEvents() {
        // ARRANGE
        Member member = new Member();
        member.setName("Test Member Walsh");

        Band band = new Band();
        band.setName("Original Band Name");
        band.setMembers(Set.of(member));

        Event originalEvent = new Event();
        originalEvent.setTitle("Original Title");
        originalEvent.setBands(Set.of(band));

        List<Event> allEvents = List.of(originalEvent);
        when(eventRepository.findAll()).thenReturn(allEvents);

        // ACT
        eventService.getFilteredEvents("Walsh");

        // ASSERT - Verify that original objects are not modified
        assertEquals("Original Title", originalEvent.getTitle(),
                "Original title should remain unchanged");
        assertEquals("Original Band Name", band.getName(),
                "Original band name should remain unchanged");
    }

    @Test
    void getFilteredEvents_ShouldAddCorrectCountsForMultipleScenarios() {
        // ARRANGE - Create different scenarios
        // Event 1: 3 bands with varying member counts
        Member m1 = new Member();
        m1.setName("Walsh Member");

        Band band1 = new Band(); // 1 member
        band1.setName("Band One");
        band1.setMembers(Set.of(m1));

        Band band2 = new Band(); // 3 members
        band2.setName("Band Two");
        band2.setMembers(Set.of(m1, new Member(), new Member()));

        Band band3 = new Band(); // 2 members
        band3.setName("Band Three");
        band3.setMembers(Set.of(m1, new Member()));

        Event event = new Event();
        event.setTitle("Multi Band Event");
        event.setBands(Set.of(band1, band2, band3));

        when(eventRepository.findAll()).thenReturn(List.of(event));

        // ACT
        List<Event> result = eventService.getFilteredEvents("Walsh");

        // ASSERT
        Event resultEvent = result.get(0);
        assertEquals("Multi Band Event [3]", resultEvent.getTitle(),
                "Should have [3] bands");

        // Check that each band has the correct count
        for (Band band : resultEvent.getBands()) {
            if (band.getName().startsWith("Band One")) {
                assertEquals("Band One [1]", band.getName());
            } else if (band.getName().startsWith("Band Two")) {
                assertEquals("Band Two [3]", band.getName());
            } else if (band.getName().startsWith("Band Three")) {
                assertEquals("Band Three [2]", band.getName());
            }
        }
    }
}
