var ip = "http://172.16.45.245:8080/"
// var ip = "http://localhost:8080/"



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
            alert(returnData.message);
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
            var isRegister = false;
            var validate = response["validate"];
            if (response["code"] == 0) {
                if (validate == "Y") {
                    let getUserURL = `${ip}webcam_web_api/api/User/${userId}`;

                    $.ajax({
                        url: getUserURL,
                        type: "GET",
                        dataType: "json",
                        async: false,
                        success: function (responseUserData) {
                            if (responseUserData.code == 0) {
                                localStorage.setItem("subordinate", responseUserData.data["subordinate"])
                                localStorage.setItem("workType", responseUserData.data["workType"])
                                localStorage.setItem("security", responseUserData.data["security"])
                                localStorage.setItem("dept", responseUserData.data["dept"])
                                localStorage.setItem("branch", responseUserData.data["branch"])
                                isRegister = true;
                            } else {
                                alert(responseUserData.message);
                            }
                        }
                    })
                }
                if ((validate == "Y") == isRegister) {
                    localStorage.setItem("userId", userId);
                    location.href = "file.html";
                }
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

function addBranch() {
    let requestURL = `${ip}webcam_web_api/api/Branch`;
    let dataJSON = {
        "branchName": $("#branchName").val(),
        "branchCode": $("#branchCode").val()
    }
    $.ajax({
        url: requestURL,
        type: "POST",
        dataType: "json",
        data: JSON.stringify(dataJSON),        
        contentType: "application/json;charset=utf-8",
        success: function(response) {
            alert(response.message);
            console.log(response);
        }
    })
}

function addWork() {
    let requestURL = `${ip}webcam_web_api/api/WorkReference`;
    let dataJSON = {
        "workName": $("#workName").val(),
        "workType": $("#workType").val()
    }
    $.ajax({
        url: requestURL,
        type: "POST",
        dataType: "json",
        data: JSON.stringify(dataJSON),        
        contentType: "application/json;charset=utf-8",
        success: function(response) {
            alert(response.message);
            console.log(response);
        }
    })
}