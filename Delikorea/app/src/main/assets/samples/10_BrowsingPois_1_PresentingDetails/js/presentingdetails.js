//아래 코드는 삭제예정

var World = {
    /* You may request new data from server periodically, however: in this sample data is only requested once. */

    userLocation: null,

    isRequestingData: false,

    initiallyLoadedData: false,

    markerDrawableIdle: null,
    markerDrawableSelected: null,
    markerDrawableDirectionIndicator: null,

    markerList: [],

    currentMarker: null,

    locationUpdateCounter: 0,
    updatePlacemarkDistancesEveryXLocationUpdates: 3,

    loadPoisFromJsonData: function loadPoisFromJsonDataFn(poiData) {

        AR.context.destroyAll();

        World.markerList = [];

        World.markerDrawableIdle = new AR.ImageResource("assets/marker_idle.png", {
            onError: World.onError
        });
        World.markerDrawableSelected = new AR.ImageResource("assets/marker_selected.png", {
            onError: World.onError
        });
        World.markerDrawableDirectionIndicator = new AR.ImageResource("assets/indi.png", {
            onError: World.onError
        });


        for (var currentPlaceNr = 0; currentPlaceNr < poiData.length; currentPlaceNr++) {
            var singlePoi = {
                "id": poiData[currentPlaceNr].id,
                "latitude": parseFloat(poiData[currentPlaceNr].y_dnts),
                "longitude": parseFloat(poiData[currentPlaceNr].x_cnts),
                "altitude": parseFloat(AR.CONST.UNKNOWN_ALTITUDE),
                "title": poiData[currentPlaceNr].upso_nm,
//                "description": poiData[currentPlaceNr].bizcnd_code_nm,
                "detail": poiData[currentPlaceNr].bizcnd_code_nm
 //               "image": poiData[currentPlaceNr].food_menu
            };

            World.markerList.push(new Marker(singlePoi));
        }

        /* Updates distance information of all placemarks. */
        /* 모든 장소 표시의 거리 정보를 업데이트합니다. */
        World.updateDistanceToUserValues();

        /*월요일날 삭제할 함수 updateStatusMessage*/
        //World.updateStatusMessage(currentPlaceNr + ' places loaded');

        AR.context.scene.cullingDistance = 150;
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

    /* Updates status message shown in small "i"-button aligned bottom center.
    삭제할 함수*/
    updateStatusMessage: function updateStatusMessageFn(message, isWarning) {

        var themeToUse = isWarning ? "e" : "c";
        var iconToUse = isWarning ? "alert" : "info";

        $("#status-message").html(message);
        $("#popupInfoButton").buttonMarkup({
            theme: themeToUse,
            icon: iconToUse
        });
    },

    /* Location updates, fired every time you call architectView.setLocation() in native environment.
    기본 환경에서 architectView.setLocation ()을 호출 할 때마다 실행되는 위치 업데이트. */
    locationChanged: function locationChangedFn(lat, lon, alt, acc) {

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
                장소 표시 거리 정보를 자주 업데이트하고 최대 10m마다 거리를 업데이트합니다.
                좀 더 노력하십시오.
             */
            World.updateDistanceToUserValues();
        }

        /* Helper used to update placemark information every now and then (e.g. every 10 location upadtes fired).
        도우미는 때때로 장소 표시 정보를 업데이트하는 데 사용되었습니다 (예 : 10 개의 위치 업데이트가 실행될 때마다).*/
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
            이 샘플에서는 캠 마커를 누르면 POI 세부 정보 패널이 나타납니다 (제목 & 설명이있는)
            샘플 디렉토리에서 index.html을 비교하십시오.
        */
        /* Update panel values. */
        $("#poi-detail-title").html(marker.poiData.title);
        $("#poi-detail-description").html(marker.poiData.detail);
        //$("#poi-detail-image").attr("src",marker.poiData.image);


        /*
            It's ok for AR.Location subclass objects to return a distance of `undefined`. In case such a distance
            was calculated when all distances were queried in `updateDistanceToUserValues`, we recalculate this
            specific distance before we update the UI.

            AR.Location 서브 클래스 객체가 '정의되지 않은'거리를 반환해도 괜찮습니다.
            'updateDistanceToUserValues'에서 모든 거리를 쿼리 할 때 이러한 거리를 계산 한 경우 UI를 업데이트하기 전에이 특정 거리를 다시 계산합니다.
         */
        if (undefined === marker.distanceToUser) {
            marker.distanceToUser = marker.markerObject.locations[0].distanceToUser();
        }

        /*
            Distance and altitude are measured in meters by the SDK. You may convert them to miles / feet if
            required.
            거리와 고도는 SDK에 의해 미터 단위로 측정됩니다. 필요한 경우 마일 / 피트로 변환 할 수 있습니다.
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
    },

    /* Screen was clicked but no geo-object was hit. */
    onScreenClick: function onScreenClickFn() {
        /* You may handle clicks on empty AR space too. */
    },


    getNumberOfVisiblePlacesInRange: function getNumberOfVisiblePlacesInRangeFn(maxRangeMeters){
        World.markerList.sort(World.sortByDistanceSorting);

        for(var i = 0; i < World.markerList.length; i++){
            if(World.markerList[i].distanceToUser > maxRangeMeters){
                return i;
            }
        }

        return World.markerList.length;
    },

    /*
        JQuery provides a number of tools to load data from a remote origin.
        It is highly recommended to use the JSON format for POI information. Requesting and parsing is done in a few lines of code.
        Use e.g. 'AR.context.onLocationChanged = World.locationChanged;' to define the method invoked on location updates.
        In this sample POI information is requested after the very first location update.

        This sample uses a test-service of Wikitude which randomly delivers geo-location data around the passed latitude/longitude user location.
        You have to update 'ServerInformation' data to use your own own server. Also ensure the JSON format is same as in previous sample's 'myJsonData.js'-file.
    */

    /* Request POI data. */
    requestDataFromServer: function requestDataFromServerFn(lat, lon) {

        /* Set helper var to avoid requesting places while loading.
        로딩하는 동안 장소를 요청하지 않도록 도우미 var를 설정하십시오.*/
        World.isRequestingData = true;

        var serverUrl = "https://graduar.s3.ap-northeast-2.amazonaws.com/testdata.json";

        var jqxhr = $.getJSON(serverUrl, function(data) {
                World.loadPoisFromJsonData(data);
            })
            .error(function(err) {
                World.updateStatusMessage("Invalid web-service response.", true);
                World.isRequestingData = false;
            })
            .complete(function() {
                World.isRequestingData = false;
            });
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