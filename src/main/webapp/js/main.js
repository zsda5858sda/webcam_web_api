// $(function () {
//     let requestURL = "http://localhost:8080/webcam_web_api/WorkReference";
//     $.ajax({
//         url: requestURL,
//         type: "GET",
//         dataType: "json",
//         contentType: "application/json;charset=utf-8",
//         success: function (returnData) {
//             console.log(returnData);
//             var dataList = returnData['data'];
//             for (var i = 0; i < dataList.length; i++) {
//                 $('#dept').append($('<option>', {
//                     value: dataList[i]['workType'],
//                     text: dataList[i]['workName']
//                 }));
//             }
//         },
//         error: function (xhr, ajaxOptions, thrownError) {
//             console.log(xhr.status);
//             console.log(thrownError);
//         }
//     });
// })

function submit() {
    let requestURL = "http://localhost:8080/webcam_web_api/User";
    let dataJSON = {
        "userId": $("#uid").val(),
        "manager": $('input[name=manager]:checked').val(),
        "dept": $("#dept").val(),
        "branch": $("#branch").val(),
        "workType": $("#workType").val()
    }
    $.ajax({
        url: requestURL,
        data: JSON.stringify(dataJSON),
        type: "POST",
        dataType: "json",
        contentType: "application/json;charset=utf-8",
        success: function (returnData) {
            console.log(returnData);
        },
        error: function (xhr, ajaxOptions, thrownError) {
            console.log(xhr.status);
            console.log(thrownError);
        }
    });
}