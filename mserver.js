var app = require('express')();
var server = require('http').createServer(app);
var io = require('socket.io')(server);

var getJSON = require('get-json');

var serverUrl = "https://graduar.s3.ap-northeast-2.amazonaws.com/testdata.json";

var userlat;
var userlon;

function getDistanceFromLatLonInKm(lat1,lng1,lat2,lng2) {
    function deg2rad(deg) {
        return deg * (Math.PI/180)
    }

    var R = 6371; // Radius of the earth in km
    var dLat = deg2rad(lat2-lat1);  // deg2rad below
    var dLon = deg2rad(lng2-lng1);
    var a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.sin(dLon/2) * Math.sin(dLon/2);
    var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    var d = R * c; // Distance in km
    return d;
}

app.get('/', function (req, res) {
    console.log('success');
});
//커넥션이 수립되면 소켓까지 들어옴 1번
io.on('connection', function (socket) {
    //location까지 보내줌
    socket.on('location', function (data) {
        userlat = data.lat;
        userlon = data.lon;
    });
});
getJSON(serverUrl, function (error, response) {
    for (var currentPlaceNr = 0; currentPlaceNr < response.length; currentPlaceNr++) {
        var singlePoi = {
            "latitude": parseFloat(response[currentPlaceNr].y_dnts),
            "longitude": parseFloat(response[currentPlaceNr].x_cnts),
            "altitude": parseFloat(AR.CONST.UNKNOWN_ALTITUDE),
            "title": response[currentPlaceNr].upso_nm,
            "description": response[currentPlaceNr].bizcnd_code_nm
        };
        
        var userdistance = getDistanceFromLatLonInKm(userlat,userlon,singlePoi.latitude,singlePoi.longitude);
        //setdistance가 500안쪽이면 
        if(userdistance <= 500){
            io.emit('setPoi',singlePoi);
        }
    }
});
server.listen(3000, function () {
    console.log('socket IO server listening on port 3000');
});