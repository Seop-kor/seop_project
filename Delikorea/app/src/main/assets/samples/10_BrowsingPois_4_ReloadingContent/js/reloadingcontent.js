/* Implementation of AR-Experience (aka "World"). */
var transed = [];
var target;
var World = {

    /*
        User's latest known location, accessible via userLocation.latitude, userLocation.longitude,
         userLocation.altitude.
     */

    userLocation: null,

    /* You may request new data from server periodically, however: in this sample data is only requested once. */
    isRequestingData: false,

    /* True once data was fetched. */
    initiallyLoadedData: false,

    /* Different POI-Marker assets. */
    markerDrawableIdle: null,
    markerDrawableSelected: null,
    markerDrawableDirectionIndicator: null,

    /* List of AR.GeoObjects that are currently shown in the scene / World. */
    markerList: [],

    needtrans: [],

    /* the last selected marker. */
    currentMarker: null,

    locationUpdateCounter: 0,
    updatePlacemarkDistancesEveryXLocationUpdates: 3,

    /* Called to inject new POI data. */
    loadPoisFromJsonData: function loadPoisFromJsonDataFn(poiData) {

        /* Destroys all existing AR-Objects (markers & radar). */
        AR.context.destroyAll();

        /* Empty list of visible markers. */
        World.markerList = [];

        World.needtrans = [];

        /* Start loading marker assets. */
        World.markerDrawableIdle = new AR.ImageResource("assets/marker_idle.png", {
            onError: World.onError
        });
        World.markerDrawableSelected = new AR.ImageResource("assets/marker_selected.png", {
            onError: World.onError
        });
        World.markerDrawableDirectionIndicator = new AR.ImageResource("assets/indi.png", {
            onError: World.onError
        });

        AR.context.scene.cullingDistance = 150;


        for(var i = 0; i < poiData.length; i++){
            World.needtrans.push(poiData[i].upso_nm);
            World.needtrans.push(poiData[i].bizcnd_code_nm);
            World.needtrans.push(poiData[i].rdn_code_nm);
        }
        World.translating(World.needtrans,World.updatePoi,poiData);
    },

    // translating: function translatingFn(str,func,poiData){
    //     transed = [];
    //     var promise = function(){
    //         return new Promise(function(resolve,reject){
    //             let cnt = 0;
    //             for(var i = 0; i < str.length; i++){
    //                 $.ajax({
    //                     type: 'GET',
    //                     url: 'http://13.125.180.217:3000/translate?text='+str[i],
    //                     async:false,
    //                     success: function(result){
    //                         // let endjson = JSON.parse(result);
    //                         World.setData(result);
    //                     }
    //                 });
    //                 cnt++;
    //             }
    //             if(cnt == str.length){
    //                 console.log(transed);
    //                 resolve(transed);
    //             }
    //         });
    //     }
        
    //     promise().then(function(data){
    //         func(poiData,data);
    //     });
        
    // },
    translating: function translatingFn(str, func, poiData) {
        for (var i = 0; i < str.length; i++) {
          $.ajax({
            type: "GET",
            async: false,
            url: "http://13.125.180.217:3000/translate?text=" + str[i] + "&target="+target,
            success: function(result) {
              // let endjson = JSON.parse(result);
              transed.push(result);
            }
          });
        }
        func(poiData, transed);
    },

    updatePoi: function updatePoiFn(poiData,data){
        for (var currentPlaceNr = 0; currentPlaceNr < poiData.length; currentPlaceNr++) {
            var singlePoi = {
                "id": poiData[currentPlaceNr].id,
                "latitude": parseFloat(poiData[currentPlaceNr].y_dnts),
                "longitude": parseFloat(poiData[currentPlaceNr].x_cnts),
                "altitude": parseFloat(AR.CONST.UNKNOWN_ALTITUDE),
                "title": data[3*currentPlaceNr],
                "description": data[3*currentPlaceNr+1],
                "address": data[3*currentPlaceNr+2]
            };

            World.markerList.push(new Marker(singlePoi));
        }
        World.updateDistanceToUserValues();
    },

    /*
        Sets/updates distances of all makers so they are available way faster than calling (time-consuming)
        distanceToUser() method all the time.
     */
    updateDistanceToUserValues: function updateDistanceToUserValuesFn() {
        for (var i = 0; i < World.markerList.length; i++) {
            World.markerList[i].distanceToUser = World.markerList[i].markerObject.locations[0].distanceToUser();
        }
    },

    /* Location updates, fired every time you call architectView.setLocation() in native environment. */
    locationChanged: function locationChangedFn(lat, lon, alt, acc) {

        /* Store user's current location in World.userLocation, so you always know where user is. */
        World.userLocation = {
            'latitude': lat,
            'longitude': lon,
            'altitude': alt,
            'accuracy': acc
        };


        /* Request data if not already present. */
        if (!World.initiallyLoadedData) {
            World.requestDataFromServer(lat, lon);
            World.initiallyLoadedData = true;
        } else if (World.locationUpdateCounter === 0) {
            /*
                Update placemark distance information frequently, you max also update distances only every 10m with
                some more effort.
             */
            World.updateDistanceToUserValues();
        }

        /* Helper used to update placemark information every now and then (e.g. every 10 location upadtes fired). */
        World.locationUpdateCounter =
            (++World.locationUpdateCounter % World.updatePlacemarkDistancesEveryXLocationUpdates);
    },

    /*
        POIs usually have a name and sometimes a quite long description.
        Depending on your content type you may e.g. display a marker with its name and cropped description but
        allow the user to get more information after selecting it.
    */

    /* Fired when user pressed maker in cam. */
    onMarkerSelected: function onMarkerSelectedFn(marker) {
        World.currentMarker = marker;

        /*
            In this sample a POI detail panel appears when pressing a cam-marker (the blue box with title &
            description), compare index.html in the sample's directory.
        */
        /* Update panel values. */
        $("#poi-detail-title").html(marker.poiData.title);
        $("#poi-detail-description").html(marker.poiData.description);
        $("#poi-detail-menu").html(marker.poiData.address);

        /*
            It's ok for AR.Location subclass objects to return a distance of `undefined`. In case such a distance
            was calculated when all distances were queried in `updateDistanceToUserValues`, we recalculate this
            specific distance before we update the UI.
         */
        if (undefined === marker.distanceToUser) {
            marker.distanceToUser = marker.markerObject.locations[0].distanceToUser();
        }

        /*
            Distance and altitude are measured in meters by the SDK. You may convert them to miles / feet if
            required.
        */
        var distanceToUserValue = (marker.distanceToUser > 999) ?
            ((marker.distanceToUser / 1000).toFixed(2) + " km") :
            (Math.round(marker.distanceToUser) + " m");

        $("#poi-detail-distance").html(distanceToUserValue);

        /* Show panel. */
        $("#panel-poidetail").panel("open", 123);

        $(".ui-panel-dismiss").unbind("mousedown");

        /* Deselect AR-marker when user exits detail screen div. */
        $("#panel-poidetail").on("panelbeforeclose", function(event, ui) {
            World.currentMarker.setDeselected(World.currentMarker);
        });
        $("#poi-detail-navigation").click(function(){
            AR.platform.sendJSONObject({
                startlat: World.userLocation.latitude,
                startlon: World.userLocation.longitude,
                endlat: marker.poiData.latitude,
                endlon: marker.poiData.longitude
            });
        });
    },

    /* Screen was clicked but no geo-object was hit. */
    onScreenClick: function onScreenClickFn() {
        /* You may handle clicks on empty AR space too. */
    },

    /*
        You may need to reload POI information because of user movements or manually for various reasons.
        In this example POIs are reloaded when user presses the refresh button.
        The button is defined in index.html and calls World.reloadPlaces() on click.
    */

    /* Request POI data. */
    requestDataFromServer: function requestDataFromServerFn(lat, lon) {

        /* Set helper var to avoid requesting places while loading. */
        World.isRequestingData = true;

        /* Server-url to JSON content provider. */
        var serverUrl = "https://graduar.s3.ap-northeast-2.amazonaws.com/appdata.json";
        // var serverUrl = "https://graduar.s3.ap-northeast-2.amazonaws.com/testdata.json";

        var jqxhr = $.getJSON(serverUrl, function(data) {
                World.loadPoisFromJsonData(data);
            })
            .error(function(err) {
                World.isRequestingData = false;
            })
            .complete(function() {
                World.isRequestingData = false;
            });
    },

    settest: function settestFn(str){
        target = str;
    },

    /* Helper to sort places by distance. */
    sortByDistanceSorting: function sortByDistanceSortingFn(a, b) {
        return a.distanceToUser - b.distanceToUser;
    },

    /* Helper to sort places by distance, descending. */
    sortByDistanceSortingDescending: function sortByDistanceSortingDescendingFn(a, b) {
        return b.distanceToUser - a.distanceToUser;
    },

    onError: function onErrorFn(error) {
        alert(error);
    }
};


/* Forward locationChanges to custom function. */
AR.context.onLocationChanged = World.locationChanged;

/* Forward clicks in empty area to World. */
AR.context.onScreenClick = World.onScreenClick;