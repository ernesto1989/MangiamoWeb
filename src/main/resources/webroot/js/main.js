function creaNuevaQuincena(){
    w2popup.close();
    var dia = $('#Quincena').val();
    var json = "{\"dia\":"+ dia + "}";
    $.ajax({
        url: "/api/nueva_quincena",
        type: 'POST',
        data: json,
        contentType: 'application/json',
        success: function (data) {
            w2alert("Quincena creada").done(function () {
               location.reload();
            });
        },
        error: function (request, status, error) {
            w2alert("Ocurrió un error al obtener los datos. Error: " + request.responseText);
        }
    });
}

function fechaNuevaQuincena() {
    w2confirm("Desea crear una nueva quincena?", function (answer) {
        if(answer == 'Yes'){
            w2popup.open({
                width: 250,
                height: 250,
                title: 'Nueva Quincena',
                body: '<div class="w2ui-centered" style="line-height: 1.8">' +
                        '     Nueva Quincena<br><br>' +
                        '     Fecha inicio: <select id="Quincena" class="w2ui-input" style="margin-bottom: 5px">'+
                        '                   <option default>10</option>'+
                        '                   <option>25</option>'+
                        '                   </select>'+
                        '</div>',
                buttons: '<button class="w2ui-btn" onclick="creaNuevaQuincena()">Ok</button>' +
                        '<button class="w2ui-btn" onclick="w2popup.close()">Cancel</button>'
            });
        }

    });
};


function saveGasto() {
    w2popup.close();
    var id = $('#Id').val();
    var nombre = $('#Descripcion').val();
    var total = $('#Total').val();
    var operacion = 'POST';
    var update = false;

    var gq = "{\"type\":\"gq\",";
    if (id != null) {
        update = true;
        operacion = 'PUT';
        gq += '"recid":' + id + ',';
    }
    gq += "\"descripcion\":\"" + nombre + "\",";
    gq += "\"total\":" + total * -1 + "}";

    $.ajax({
        url: '/api',
        type: operacion,
        contentType: 'application/json',
        data: gq,
        dataType: 'json',
        success: function (data) {
            var id = data.recid;
            w2ui.grid.add($.extend(true, {recid: id}, data));
            w2ui.grid.selectNone();
        },
        error: function (request, status, error) {
            w2alert("Ocurrió un error al agregar un nuevo GF. Error: " + request.responseText);
        }
    });
}

function getResumen(){
    //Traer todos los valores de inicio de la pantalla.
    $.ajax({
        url: "/api/resumen",
        type: 'GET',
        contentType: 'application/json',
        success: function (data) {
            var resumen = data[0];
            w2ui.form.record = resumen;
            w2ui.form.refresh();
        },
        error: function (request, status, error) {
            //w2alert("Ocurrió un error al obtener los datos. Error: " + request.responseText);
        }
    });                    
}

function getBitacoraRecords() {
    $.ajax({
        url: "/api/bitacora",
        type: 'GET',
        contentType: 'application/json',
        success: function (data) {
            w2ui.grid.records = data;
            w2ui.grid.refresh();
        },
        error: function (request, status, error) {
            w2alert("Ocurrió un error al obtener los datos. Error: " + request.responseText);
        }
    });
}

function registerHandler() {
    eventBus = new EventBus('/websocket-connection');
    eventBus.onopen = function () {
        eventBus.registerHandler('out', function (error, message) {
            const msg = message.body;
            console.log(msg.action);
        });

        //función que muestra el ejemplo de envío de mensajes al servidor
        window.setInterval(function(){
            eventBus.send("in",'{"address":"eb_incomming_msg","action":"do_action","entity":{"msg":"test"}}');
        },10000);

    }
}
