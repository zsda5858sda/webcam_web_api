var ip = "http://172.16.45.245:8080/"

function submit() {
    var dept = $("#dept").val();
    var branch = $("#branch").val();
    let requestURL = `${ip}webcam_web_api/api/User`;
    let dataJSON = {
        "userId": $("#uid").val(),
        "manager": $('input[name=manager]:checked').val(),
        "security": $('input[name=security]:checked').val(),
        "dept": dept,
        "branch": branch,
    }
    var workType = "";
    $('input[name=workType]:checked').each(function () {
        workType += this.value + ";"
    });
    if (branch.match("800|100|600")) {
        workType = "ALL";
    }
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
            if (response["code"] == 0) {
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