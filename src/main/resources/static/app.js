'use strict';

angular.module('myevent', [
    'angular-input-stars'
])
    .factory('EventService', EventService)
    .controller('EventsController', ['EventService', '$http', '$scope', EventsController]);

function EventService($http) {
    return {
        deleteEvent: deleteEvent,
        getEvents: getEvents,
        updateStars: updateStars
    };

    function deleteEvent(id) {
        return $http.delete('/api/events/' + id);
    }

    function getEvents() {
        return $http.get('/api/events/')
            .then(getEventsComplete);

        function getEventsComplete(response) {
            return response.data;
        }
    }

    function updateStars(event) {
        return $http.put('/api/events/' + event.id, event);
    }
}

function EventsController(EventService, $http, $scope) {
    var vm = this;
    vm.deleteEvent = deleteEvent;
    vm.updateStars = updateStars;
    vm.search = search;
    vm.showAll = showAll;
    vm.searchQuery = '';

    activate();

    function activate() {
        return EventService.getEvents()
            .then(function (events) {
                vm.events = events;
                return vm.events;
            });
    }

    function search() {
        if (vm.searchQuery && vm.searchQuery.trim()) {
            $http.get('/api/events/search/' + encodeURIComponent(vm.searchQuery.trim()))
                .then(function (response) {
                    vm.events.splice(0, vm.events.length);
                    Array.prototype.push.apply(vm.events, response.data);
                    $scope.$apply();
                })
                .catch(function (error) {
                    console.error('Erreur de recherche:', error);
                });
        }
    }

    function showAll() {
        vm.searchQuery = '';
        activate();
    }

    function deleteEvent(event) {
        var index = vm.events.indexOf(event);
        return EventService.deleteEvent(event.id)
            .then(function () {
                vm.events.splice(index, 1);
            });
    }

    function updateStars(event) {
        return EventService.updateStars(event);
    }
}