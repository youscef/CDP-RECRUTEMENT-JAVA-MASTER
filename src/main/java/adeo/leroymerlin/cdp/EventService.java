package adeo.leroymerlin.cdp;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public List<Event> getEvents() {
        return eventRepository.findAll();
    }

    public void delete(Long id) {
        eventRepository.deleteById(id);
    }

    public List<Event> getFilteredEvents(String query) {
        List<Event> events = eventRepository.findAll();
        // Filter the events list in pure JAVA here
        List<Event> filteredEvents = events.stream()
                .filter(event -> hasMatchingMember(event, query))
                .collect(Collectors.toList());
        return addCountsToEvents(filteredEvents);
    }

    private boolean hasMatchingMember(Event event, String query) {
        if (event.getBands() == null || query == null) {
            return false;
        }

        return event.getBands().stream()
                .filter(band -> band.getMembers() != null)
                .flatMap(band -> band.getMembers().stream())
                .map(member -> member.getName())
                .filter(name -> name != null)
                .anyMatch(name -> name.toLowerCase()
                .contains(query.toLowerCase()));
    }

    private List<Event> addCountsToEvents(List<Event> events) {
        return events.stream()
                .map(event -> {

                    Event eventWithCount = new Event();
                    eventWithCount.setId(event.getId());
                    eventWithCount.setImgUrl(event.getImgUrl());
                    eventWithCount.setNbStars(event.getNbStars());
                    eventWithCount.setComment(event.getComment());

                    if (event.getBands() != null) {
                        int bandCount = event.getBands().size();
                        eventWithCount.setTitle(event.getTitle() + " [" + bandCount + "]");

                        Set<Band> bandsWithCounts = event.getBands().stream()
                                .map(band -> {
                                    Band bandWithCount = new Band();
                                    bandWithCount.setMembers(band.getMembers());

                                    if (band.getMembers() != null) {
                                        int memberCount = band.getMembers().size();
                                        bandWithCount.setName(band.getName() + " [" + memberCount + "]");
                                    } else {
                                        bandWithCount.setName(band.getName() + " [0]");
                                    }

                                    return bandWithCount;
                                })
                                .collect(Collectors.toSet());

                        eventWithCount.setBands(bandsWithCounts);
                    } else {
                        eventWithCount.setTitle(event.getTitle() + " [0]");
                    }

                    return eventWithCount;
                })
                .collect(Collectors.toList());
    }

    // Update the event with the given ID
    public void updateEvent(Long id, Event eventUpdate) {
        Event existingEvent = eventRepository.findById(id).orElse(null);
        if (existingEvent != null) {
            // ONLY update the fields we care about
            if (eventUpdate.getNbStars() != null) {
                existingEvent.setNbStars(eventUpdate.getNbStars());
            }
            if (eventUpdate.getComment() != null) {
                existingEvent.setComment(eventUpdate.getComment());
            }

            eventRepository.save(existingEvent);
        }
    }

}
