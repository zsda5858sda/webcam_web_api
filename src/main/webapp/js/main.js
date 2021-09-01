function submit() {
    let requestURL = "http://localhost:8080/webcam_web_api/api/User";
    let dataJSON = {
        "userId": $("#uid").val(),
        "manager": $('input[name=manager]:checked').val(),
        "dept": $("#dept").val(),
        "branch": $("#branch").val()
    }
    var workType = "";
    $('input[name=workType]:checked').each(function(){
        workType += this.value + ";"
    });
    dataJSON.workType = workType;
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
function search() {
    
    var minDate = $("#minDate").val().replaceAll("-", "");
    var maxDate = $("#maxDate").val().replaceAll("-", "");
    var userId = $("#uid").val();
    var workType = "ALL";
    let requestURL = `http://localhost:8080/webcam_web_api/api/File?minDate=${minDate}&maxDate=${maxDate}&userId=${userId}&workType=${workType}`;
    $.ajax({
        url: requestURL,
        dataType: "json",
        type: "GET",
        success: function (returnData) {
            
        },
        error: function (xhr, thrownError) {
            console.log(xhr.status);
            console.log(thrownError);
        }
    });
}

function login() {
    var userId = $("#uid").val();
    var pwd = $("#password").val();
    let loginURL = `${ip}webcam_web_api/api/getAD`;
    let dataJSON = {
        "loginId": userId,
        "loginP_ss": pwd
    }
    localStorage.removeItem("subordinate");
    localStorage.removeItem("workType");
    $.ajax({
        url: loginURL,
        data: JSON.stringify(dataJSON),
        dataType: "json",
        type: "POST",
        contentType: "application/json;charset=utf-8",
        success: function (response) {
            if (response["code"] == "0") {
                if (response["validate"] == "Y") {
                    let getUserURL = `${ip}webcam_web_api/api/User/${userId}`;
                    
                    $.ajax({
                        url: getUserURL,
                        type: "GET",
                        dataType: "json",
                        async: false,
                        success: function (data) {
                            localStorage.setItem("subordinate", data.data["subordinate"])
                            localStorage.setItem("workType", data.data["workType"])
                            localStorage.setItem("security", data.data["security"])
                            localStorage.setItem("dept", data.data["dept"])
                            localStorage.setItem("branch", data.data["branch"])
                        }
                    })
                }
                localStorage.setItem("userId", userId);
                location.href = "file.html";
            } else {
                console.log(response);
                alert(response.message);
            }
        },
        error: function (xhr) {
            console.log(xhr.status);
            alert(xhr.responseJSON.message);
        }
    });
}