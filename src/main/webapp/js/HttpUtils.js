class HttpUtils {
    static getByData = async (url, data) => {
        let token = sessionStorage.getItem("token") ?? "";
        const response = await fetch(url, {
            method: "GET",
            body: JSON.stringify(data),
            headers: {
                'Authorization': `Bearer ${token}`,
                'content-type': 'application/json;charset=utf-8'
            }
        });
        return await response.json();
    }

    static get = async (url) => {
        let token = sessionStorage.getItem("token") ?? "";
        const response = await fetch(url, {
            method: "GET",
            headers: {
                'Authorization': `Bearer ${token}`,
                'content-type': 'application/json;charset=utf-8'
            }
        });
        return await response.json();
    }

    static post = async (url, data) => {
        let token = sessionStorage.getItem("token") ?? "";
        const response = await fetch(url, {
            method: "POST",
            body: JSON.stringify(data),
            headers: {
                'Authorization': `Bearer ${token}`,
                'content-type': 'application/json;charset=utf-8'
            }
        });
        return await response.json();
    }

    static patch = async (url, data) => {
        let token = sessionStorage.getItem("token") ?? "";
        const response = await fetch(url, {
            method: "PATCH",
            body: JSON.stringify(data),
            headers: {
                'Authorization': `Bearer ${token}`,
                'content-type': 'application/json;charset=utf-8'
            }
        });
        return await response.json();
    }

    static postFormdata = async (url, formdata) => {
        let token = sessionStorage.getItem("token") ?? "";
        const response = await fetch(url, {
            method: "POST",
            body: formdata,
            headers: {
                'Authorization': `Bearer ${token}`,
            }
        });
        return await response.json();
    }

    static getBlob = async (url) => {
        let token = sessionStorage.getItem("token") ?? "";
        const response = await fetch(url, {
            method: "GET",
            headers: {
                'Authorization': `Bearer ${token}`,
                'content-type': 'application/json;charset=utf-8'
            }
        });
        return await response.blob();
    }
}