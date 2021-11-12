var ip = "http://172.16.45.245:8080/"
// var ip = "http://192.168.141.207:8080/";

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
    localStorage.removeItem("workType");
    $.ajax({
        url: loginURL,
        data: JSON.stringify(dataJSON),
        dataType: "json",
        type: "POST",
        contentType: "application/json;charset=utf-8",
        success: async function (response) {
            await sendLog(userId, response.message);
            if (response["code"] == 0) {
                let getUserURL = `${ip}webcam_web_api/api/User/${userId}`;
                localStorage.clear();
                $.ajax({
                    url: getUserURL,
                    type: "GET",
                    dataType: "json",
                    async: false,
                    success: function (responseUserData) {
                        if (responseUserData.code == 0) {
                            localStorage.setItem("workType", responseUserData.data["workType"])
                            localStorage.setItem("security", responseUserData.data["security"])
                            localStorage.setItem("dept", responseUserData.data["dept"])
                            localStorage.setItem("branch", responseUserData.data["branch"])
                            localStorage.setItem("manager", responseUserData.data["manager"])
                            localStorage.setItem("appointed", responseUserData.data["appointed"])
                        }
                    }
                })
                localStorage.setItem("userId", userId);
                location.href = "index.html";
            } else {
                alert(response.message);
            }
        },
        error: function (xhr) {
            alert(xhr.responseJSON.message);
        }
    });
}

//查詢檔案api
function searchFile() {
    var minDate = $("#minDate").val().replace(/-/g, "");
    var maxDate = $("#maxDate").val().replace(/-/g, "");
    if (minDate == "") {
        alert("請輸入最小日期")
        return;
    }
    if (maxDate == "") {
        alert("請輸入最大日期")
        return;
    }
    var userId = $("#uid").val();
    var branch = localStorage.getItem("branch");
    var workType = localStorage.getItem("workType");
    let requestURL = `${ip}webcam_web_api/api/File?minDate=${minDate}&maxDate=${maxDate}&userId=${userId}&branch=${branch || ""}&workType=${workType || ""}`;
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
    var branchName = $("#branchName").val();
    var branchCode = $("#branchCode").val();
    if (branchName.replaceAll(" ", "") == "") {
        alert("請輸入分行名稱")
        return;
    }
    if (branchCode.replaceAll(" ", "") == "") {
        alert("請輸入分行代號")
        return;
    }
    let requestURL = `${ip}webcam_web_api/api/Branch`;
    let dataJSON = {
        "branchName": branchName,
        "branchCode": branchCode
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
    var workName = $("#workName").val();
    var workType = $("#workType").val();
    if (workName.replaceAll(" ", "") == "") {
        alert("請輸入業務種類名稱")
        return;
    }
    if (workType.replaceAll(" ", "") == "") {
        alert("請輸入業務種類代號")
        return;
    }
    let requestURL = `${ip}webcam_web_api/api/WorkReference`;
    let dataJSON = {
        "workName": workName,
        "workType": workType
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
            { name: 'userId', index: 'userId', with: 200 },
            { name: 'createDatetime', index: 'createDatetime', width: 250 },
            { name: 'action', index: 'action', width: 550 },
            { name: 'ip', index: 'ip', width: 200 },
        ],
        datatype: "local",
        data: responseData,
        width: $(window).width() - 300,
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
    if ($('input[name=workType]:checked').val() == null && dept == branch && dept == '600') {
        alert("請勾選業務種類");
        return;
    }
    var workType = "", workName = "";
    var manager = $('input[name=manager]:checked').val();
    var appointed = $('input[name=appointed]:checked').val();
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
        "appointed": appointed,
        "security": security,
        "dept": dept,
        "branch": branch,
        "workType": workType
    }
    let dataNameJSON = {
        "userId": userId,
        "manager": manager == "N" ? "否" : "是",
        "appointed": appointed == "N" ? "否" : "是",
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

function goCheckWork(type) {

    var workType = $("#workType").val().replaceAll(" ", "");
    var workName = $("#workName").val().replaceAll(" ", "");
    if (workType == "") {
        alert("請輸入業務種類代號")
        return;
    }
    if (workName == "") {
        alert("請輸入業務種類名稱")
        return;
    }
    var dataJSON = {
        "workType": workType,
        "workName": workName,
    }
    if (type == 'U') {
        var oldKey = $("#oldWorkType").val();
        var oldName = $("#oldWorkName").select2('data')[0].text;
        dataJSON["oldKey"] = oldKey;
        dataJSON["oldName"] = oldName;
    }

    localStorage.setItem("checkData", JSON.stringify(dataJSON));


}

function goCheckBranch(type) {
    var branchCode = $("#branchCode").val().replaceAll(" ", "");
    var branchName = $("#branchName").val().replaceAll(" ", "");
    if (branchCode == "") {
        alert("請輸入分行代號")
        return;
    }
    if (branchName == "") {
        alert("請輸入分行名稱")
        return;
    }
    var dataJSON = {
        "branchCode": branchCode,
        "branchName": branchName,
    }
    if (type == 'U') {
        var oldKey = $("#oldBranchCode").val();
        var oldName = $("#oldBranchName").select2('data')[0].text;
        dataJSON["oldKey"] = oldKey;
        dataJSON["oldName"] = oldName;
    }

    localStorage.setItem("checkData", JSON.stringify(dataJSON));
}


// 更新資料api，type為欲更改之類型，用來連接api
function update(type) {

    $("#submitBtn").attr("disabled", true);
    let requestURL = `${ip}webcam_web_api/api/${type}`;
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