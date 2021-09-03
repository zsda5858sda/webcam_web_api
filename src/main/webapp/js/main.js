// var ip = "http://172.16.45.245:8080/"
var ip = "http://localhost:8080/";



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
    if (branch == "800") {
        dataJSON.security = "AU";
    }
    dataJSON.workType = workType;
    $.ajax({
        url: requestURL,
        data: JSON.stringify(dataJSON),
        type: "POST",
        dataType: "json",
        contentType: "application/json;charset=utf-8",
        success: async function (response) {
            await sendLog(localStorage.getItem("userId"), response.message);
            console.log(response);
            alert(response.message);
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
        success: async function (response) {
            var isRegister = false;
            var validate = response["validate"];
            localStorage.setItem("validate", validate);
            await sendLog(userId, response.message);
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
                    location.href = "index.html";
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

function updateValid() {
    $.ajax({
        url: `${ip}webcam_web_api/api/updateValid`,
        type: "PATCH",
        dataType: "json",
        data: JSON.stringify({
            "validate": $("#validate").val()
        }),
        contentType: "application/json;charset=utf-8",
        success: async function (response) {
            await sendLog(localStorage.getItem("userId"), response.message);
            alert(response.message);
            console.log(response);
        }
    })
}

function searchFile() {
    var minDate = $("#minDate").val().replace(/-/g, "");
    var maxDate = $("#maxDate").val().replace(/-/g, "");
    var userId = $("#uid").val();
    var workType = localStorage.getItem("workType");
    let requestURL = `${ip}webcam_web_api/api/File?minDate=${minDate}&maxDate=${maxDate}&userId=${userId}&workType=${workType}`;
    $.ajax({
        url: requestURL,
        dataType: "json",
        type: "GET",
        success: async function (response) {
            await sendLog(localStorage.getItem("userId"), response.message);
            if (response.code == 0) {
                $(".search_container").hide();
                $(".file_manager").show();
                responseData = response.data;
                if (responseData.length != 0) {
                    for (i = 0; i < responseData.length; i++) {
                        userArr.push(responseData[i]["fileName"].substring(0, 7));
                    }
                    userArr = userArr.filter(function (data, index, arr) {
                        return arr.indexOf(data) === index;
                    });
                    console.log(userArr);
                    if (userArr.length == 1) {
                        localStorage.setItem("selectedUser", userArr[0]);
                        for (i = 0; i < responseData.length; i++) {
                            dateArr.push(responseData[i]["workDate"]);
                        }
                        dateArr = dateArr.filter(function (data, index, arr) {
                            return arr.indexOf(data) === index;
                        })
                        console.log(dateArr);
                        dateArr.forEach(function (data) {
                            appendFolderContainer(data)
                        })
                    } else {
                        userArr.forEach(function (data) {
                            appendFolderContainer(data)
                        })
                    }
                } else {
                    location.reload();
                    alert("查無資料")
                }
            } else {
                alert(response.message);
            }
        },
        error: function (xhr, thrownError) {
            console.log(xhr.status);
            console.log(thrownError);
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
        success: async function (response) {
            await sendLog(localStorage.getItem("userId"), response.message);
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
        success: async function (response) {
            await sendLog(localStorage.getItem("userId"), response.message);
            alert(response.message);
            console.log(response);
        }
    })
}

async function sendLog(userId, action) {
    var userIp = (await $.getJSON("https://api.ipify.org/?format=json")).ip;
    let requestURL = `${ip}webcam_web_api/api/Log`
    var dataJSON = {
        "userId": userId,
        "action": action,
        "ip": userIp
    }
    $.ajax({
        url: requestURL,
        type: "POST",
        dataType: "json",
        data: JSON.stringify(dataJSON),
        contentType: "application/json;charset=utf-8",
        success: function (response) {
            console.log(response);
        }
    })
}

async function searchLog(isApp) {
    var minDate = $("#minDate").val().replace(/-/g, "");
    var maxDate = $("#maxDate").val().replace(/-/g, "");
    var userId = $("#uid").val();
    var appendPath = isApp ? "/app" : "";
    let requestURL = `${ip}webcam_web_api/api/Log${appendPath}?minDate=${minDate}&maxDate=${maxDate}&userId=${userId}`
    var responseData = (await $.getJSON(requestURL)).data;
    var userIdColumnName = userId.length == 7 ? "員編" : "客戶";
    console.log(responseData);
    $.jgrid.gridUnload("#logList");
    $("#logList").jqGrid({
        colNames: [userIdColumnName, '建立時間', '事件', '來源IP'],
        colModel: [
            { name: 'userId', index: 'userId' },
            { name: 'createDatetime', index: 'createDatetime' },
            { name: 'action', index: 'action' },
            { name: 'ip', index: 'ip' },
        ],
        datatype: "local",
        data: responseData,
        width: null,
        shrinkToFit: false,
        rowNum: 10,
        rowList: [10, 20, 30],
        pager: '#logPage',
        sortname: 'createDatetime',
        viewrecords: true,
        sortorder: "desc",
    });
    $("#logList").jqGrid('navGrid', '#logPage', { edit: false, add: false, del: false });
}
