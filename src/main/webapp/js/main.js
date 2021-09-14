// var ip = "http://172.16.45.245:8080/"
var ip = "http://localhost:8080/";

//新增使用者api
function submit() {
    $("#submitBtn").attr("disabled", true);
    let requestURL = `${ip}webcam_web_api/api/User`;
    let dataJSON = localStorage.getItem("checkData");
    $.ajax({
        url: requestURL,
        data: dataJSON,
        type: "POST",
        dataType: "json",
        async: false,
        contentType: "application/json;charset=utf-8",
        success: async function (response) {
            await sendLog(localStorage.getItem("userId"), response.message);
            alert(response.message);
            $("#submitBtn").attr("disabled", false);
            localStorage.removeItem("checkData");
            history.back();
        },
    });
}

//登入api
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
                alert(response.message);
            }
        },
        error: function (xhr) {
            alert(xhr.responseJSON.message);
        }
    });
}

//更新後台使用者驗證api
function updateValid() {
    $("#updateValidBtn").attr("disabled", true);
    $.ajax({
        url: `${ip}webcam_web_api/api/updateValid`,
        type: "PATCH",
        dataType: "json",
        data: JSON.stringify({
            "validate": $("#validate").val()
        }),
        contentType: "application/json;charset=utf-8",
        success: async function (response) {
            if (response.code == 0) {
                await sendLog(localStorage.getItem("userId"), response.message);
                alert(response.message);
                $("#updateValidBtn").attr("disabled", false);
            } else {
                alert(response.message);
                $("#updateValidBtn").attr("disabled", false);
            }
        }
    })
}

//查詢檔案api
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
                let responseData = response.data;
                if (responseData.length != 0) {
                    location.href = "fileManage.html";
                    localStorage.setItem("fileData", JSON.stringify(responseData));
                } else {
                    location.reload();
                    alert("查無資料")
                }
            } else {
                alert(response.message);
            }
        },
    });
}

//新增分行、部門代碼api
function addBranch() {
    $("#addBranchBtn").attr("disabled", true);
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
            $("#addBranchBtn").attr("disabled", false);
        }
    })
}

//新增業務種類代碼api
function addWork() {
    $("#addWorkBtn").attr("disabled", true);
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
            $("#addWorkBtn").attr("disabled", false);
        }
    })
}

//新增後台log api
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

//查詢log api
async function searchLog(isApp) {
    var minDate = $("#minDate").val().replace(/-/g, "");
    var maxDate = $("#maxDate").val().replace(/-/g, "");
    if ((minDate != "" && maxDate == "") || (minDate == "" && maxDate != "")) {
        alert("請選擇查詢區間")
        return;
    }
    var userId = $("#uid").val();
    var appendPath = isApp ? "/app" : "";
    let requestURL = `${ip}webcam_web_api/api/Log${appendPath}?minDate=${minDate}&maxDate=${maxDate}&userId=${userId}`
    var responseData = (await $.getJSON(requestURL)).data;
    $.jgrid.gridUnload("#logList");
    $("#logList").jqGrid({
        colNames: ['員編 / 客戶電話', '時間', '事件描述', '來源IP'],
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

function goCheckPage(type) {

    var userId;
    var branch = $("#branch").val();
    var dept = $("#dept").val();
    if (type == "R") {
        userId = $("#uid").val().replaceAll(" ", "");
        if (userId == "") {
            alert("請輸入員編");
            return;
        }
        if (userId.length > 7) {
            alert("輸入的員編過長")
            return;
        }
    } else {
        userId = JSON.parse($("#uid").val()).userId;
    }
    if (dept == "") {
        alert("請選擇部門 / 分行");
        return;
    }
    if (branch == "") {
        alert("請選擇派駐單位");
        return;
    }
    var workType = "", workName = "";
    var manager = $('input[name=manager]:checked').val();
    var security = $('input[name=security]:checked').val();
    $('input[name=workType]:checked').each(function () {
        workType += this.value + ";"
        workName += $(`label[for=${this.value}]`).text() + " ";
    });
    if (branch.match("800|100|600")) {
        workType = "ALL";
        workName = "ALL";
    }
    if (branch == "800" && dept == "800") {
        security = "AU";
    }
    if (branch == "600" && dept == "600") {
        security = "SU";
    }
    let dataJSON = {
        "userId": userId,
        "manager": manager,
        "security": security,
        "dept": dept,
        "branch": branch,
        "workType": workType
    }
    let dataNameJSON = {
        "userId": userId,
        "manager": manager == "N" ? "否" : "是",
        "security": security == "U" ? "否" : "是",
        "dept": $('#dept :selected').text(),
        "branch": $('#branch :selected').text(),
        "workName": workName,
        "type": type
    }
    localStorage.setItem("checkData", JSON.stringify(dataJSON));
    localStorage.setItem("checkDataName", JSON.stringify(dataNameJSON));
    location.href = "checkUser.html";
}

// 更新使用者api
function updateUser() {
    $("#submitBtn").attr("disabled", true);
    let requestURL = `${ip}webcam_web_api/api/User`;
    let dataJSON = localStorage.getItem("checkData");
    $.ajax({
        url: requestURL,
        data: dataJSON,
        type: "PATCH",
        dataType: "json",
        async: false,
        contentType: "application/json;charset=utf-8",
        success: async function (response) {
            await sendLog(localStorage.getItem("userId"), response.message);
            alert(response.message);
            $("#submitBtn").attr("disabled", false);
            localStorage.removeItem("checkData");
            history.back();
        },
    });
}

// 搜尋分行、部門代碼api
function searchBranch() {
    $.ajax({
        url: `${ip}webcam_web_api/api/Branch`,
        type: "GET",
        dataType: "json",
        contentType: "application/json;charset=utf-8",
        async: false,
        success: function (returnData) {
            if (returnData.code == 0) {
                var dataList = returnData['data'];
                for (var i = 0; i < dataList.length; i++) {
                    let branchCode = dataList[i]["branchCode"];
                    let branchName = dataList[i]["branchName"];
                    $('#dept').append($('<option />', { value: branchCode, text: branchName }));
                    $('#branch').append($('<option />', { value: branchCode, text: branchName }));
                }
            } else {
                alert(returnData.message);
            }
        },
    });
}
