const token = process.argv[2];

fetch("http://localhost:8080/api/transfers", {
    method: "GET",
    headers: {
        "Authorization": `Bearer ${token}`
    }
})
.then(res => res.text().then(text => console.log(res.status, text)))
.catch(console.error);
