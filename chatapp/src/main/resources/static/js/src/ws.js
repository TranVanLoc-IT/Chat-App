'use strict';

var usernameForm = document.querySelector('#usernameForm');
var messageForm = document.querySelector('#messageForm');
var messageInput = document.querySelector('#messageInput');
var messageArea = document.querySelector('#mainChatArea');

//var connectingElement = document.querySelector('.connecting');
var stompClient = null;
var username = null;
const fileInput = document.getElementById('fileInput');
const color = document.getElementById('friendname').className.split(' ')[2].replace('text', 'bg');
const colors = ["bg-fuchsia-300", "bg-green-300", "bg-emerald-300", "bg-purple-400"];

var data = "nothing";

window.onload = function () {
    username = $('#name').text().trim();

    if (username) {

        var socket = new SockJS('/ws');// ket noi den url websocket config
        stompClient = Stomp.over(socket);

        stompClient.connect({}, onConnected, onError);
    }
    const searchBar = document.getElementById('searchBar');
    searchBar.addEventListener("click", function () {
        $("#search-container").toggleClass('invisible');
    });

    $("#addNewMember").click(function(){
        if (messageArea.className.indexOf('Group') != -1){
                
            $("#formAdd").toggleClass("invisible");
            $.ajax({
                url: '/' + id + '/getParticipantsNoJoin/' + window.location.href.split("/")[6],
                contentType: 'application/json',
                method: 'GET',
                success: function (data) {
                    var userList = $('#addMemberList');
                    userList.empty(); // Clear the existing list
                    $.each(data, function (index, e) {
                        userList.append(`<div><input type="checkbox" name="cbParticipants" value="${e.id}"><label for="">${e.username}</label></div>`);
                        
                    });
                },
                error: function (jqXHR, textStatus, errorThrown) {
                    console.log(errorThrown);
                }

            });
        }
    });
    
}
function unsend(element) {
    if (confirm("Thu hồi tin nhắn")) {
        var parent = element.closest('.block-mes');
        if (parent) {
            // Find the element with class "message" inside the parent element
            var messageElement = parent.querySelector('.message');
            var downloadIcon = parent.querySelector('.download-icon');
            var fileName = downloadIcon == null? "" : messageElement.textContent;
            var data = JSON.stringify({
                fileName: fileName,
                fileType: null,
                fileData: null,
                converId: window.location.href.split('/')[6],
                sender: null,
                message: messageElement.textContent,
                createdAt: new Date(),
                type: 'JOIN'
            });

            fetch('/unsend', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: data
            })
            .then(response => response.json())
            .then(data => {
                if (data.Result === "success") {
                    parent.innerHTML = "<p class='text-white-700 message'>Tin nhắn đã thu hồi</p>";
                } else {
                    alert("Thất bại do thời gian quá muộn");
                }
            })
            .catch(error => console.error('Error:', error));
        }
    }
}

function downloadFile(fileName, fileType, fileData) {
    const url = new URL('/download', window.location.origin);
    url.searchParams.append('fileType', fileType);
    url.searchParams.append('fileName', fileName);
    url.searchParams.append('fileData', fileData);

    fetch(url, {
        method: 'GET',
    })
    .then(response => {
        if (response.ok) {
            return response.blob();
        }
        throw new Error('Network response was not ok.');
    })
    .then(blob => {
        const downloadUrl = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = downloadUrl;
        link.download = fileName;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(downloadUrl);
    })
    .catch(error => {
        console.error('There has been a problem with your fetch operation:', error);
    });
}



function updateMessageInput() {
    var fileInput = document.getElementById('fileInput');
    var messageInput = document.getElementById('messageInput');
    var fileName = fileInput.files[0].name;
    messageInput.value = fileName;
}

function onConnected() {
    // cau hinh xu ly tin nhan
    stompClient.subscribe('/topic/public', onMessageReceived);

    // dang ky username den server
    stompClient.send("/chatapp/chatapp.addUser",
        {},
        JSON.stringify({ sender: username, type: 'JOIN' })
    )

    //connectingElement.classList.add('hidden');
}

function onError(error) {
    //connectingElement.textContent = 'Could not connect to WebSocket server. Please refresh this page to try again!';
    //connectingElement.style.color = 'red';
}


function sendMessage(event) {
    const file = fileInput.files[0];
    var converId = window.location.href.split("/")[6];
    event.preventDefault();

    var xhr = new XMLHttpRequest();
    // Cấu hình yêu cầu HTTP
    xhr.open("POST", window.location.href, true);
    xhr.setRequestHeader("Content-Type", "application/json");

    // Định nghĩa hàm callback để xử lý phản hồi
    xhr.onreadystatechange = function () {
        if (xhr.readyState === XMLHttpRequest.DONE) {
            if (xhr.status === 200) {
                // Phản hồi thành công, xử lý dữ liệu nhận được
                var data = xhr.responseText;
                // Tìm phần tử <p> có chứa messageText và thêm SVG icon vào
                var paragraphs = document.querySelectorAll("p");
                paragraphs.forEach(function (paragraph) {
                    if (paragraph.textContent.includes(messageText)) {
                        paragraph.innerHTML = messageText + `
                            <svg class="inline-block w-4 h-4 text-green-500 ml-1" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                            </svg>`;
                    }
                });
            } else {
                // Xử lý lỗi
                alert("Error: " + xhr.status);
            }
        }
    };
    // Chuẩn bị dữ liệu để gửi đi (ở dạng JSON)
    var base64File;
    if (file) {
        const reader = new FileReader();
        reader.onload = function (e) {
            base64File = e.target.result.split(',')[1]; // Get the base64 part
            data = JSON.stringify({
                fileName: file.name,
                fileType: file.name.split('.').pop().toLowerCase(),
                fileData: base64File,
                converId: converId,
                sender: username,
                message: null,
                color: color,
                createdAt: new Date,
                type: 'JOIN'
            });
            messageArea.querySelector("div:first-child").id;
            let newMessage = document.createElement("div");
            var today = new Date();
            newMessage.innerHTML = `<div>
                                    <div class="flex justify-end mb-4 cursor-pointer">
                                        <div class="flex max-w-96 ${color} text-white rounded-lg p-3 gap-3 block-mes">
                                            <i class="fas fa-file-alt text-yellow-500"></i> <!-- Icon file màu vàng -->
                                            <p class="message">${file.name}</p>
                                            <i class="fas fa-download text-blue-500 download-icon" onclick="downloadFile('${file.name}', '${file.name.split('.').pop().toLowerCase()}', '${base64File}')"></i>
                                            <p class="text-right">${today.getFullYear() + ' - ' + (today.getMonth() + 1) + ' - ' + today.getDate()}</p>
                                            <i class="fas fa-ellipsis-v options-icon" onclick='unsend(this)'></i>
                                        </div>
                                        <div class="w-9 h-9 rounded-full flex items-center justify-center ml-2">
                                        <img src="https://placehold.co/200x/b7a8ff/ffffff.svg?text=ʕ•́ᴥ•̀ʔ&font=Lato" alt="My Avatar" class="w-8 h-8 rounded-full">
                                        
                                        </div>
                                        </div>
                                    </div>`;
        messageArea.appendChild(newMessage);
            xhr.send(data);
            stompClient.send("/chatapp/chatapp.sendMessage", {}, data);
        };
        reader.readAsDataURL(file);
        messageInput.value = '';
    }
    else {
        var messageText = messageInput.value;
        messageArea.querySelector("div:first-child").id;
        let newMessage = document.createElement("div");
        var today = new Date();
        newMessage.innerHTML = `<div>
        
                                    <div class="flex justify-end mb-4 cursor-pointer">
                                    <div class="flex max-w-96 ${color} text-white rounded-lg p-3 gap-3 block-mes">
                                        <p class="message">${messageText}</p>
                                        <p class="text-right">${today.getFullYear() + ' - ' + (today.getMonth() + 1) + ' - ' + today.getDate()}</p>
                                        <i class="fas fa-ellipsis-v options-icon" onclick='unsend(this)'></i>
                                        
                                    </div>
                                    <div class="w-9 h-9 rounded-full flex items-center justify-center ml-2">
                                    <img src="https://placehold.co/200x/b7a8ff/ffffff.svg?text=ʕ•́ᴥ•̀ʔ&font=Lato" alt="My Avatar" class="w-8 h-8 rounded-full">
                                        
                                    </div>
                                    </div>
                                </div>`;
        messageArea.appendChild(newMessage);
        var messageContent = messageInput.value.trim();
        if (messageContent && stompClient) {
            data = JSON.stringify({
                fileName: null,
                fileType: null,
                fileData: null,
                sender: username,
                message: messageInput.value,
                converId: converId,
                createdAt: new Date,
                color: color,
                type: 'JOIN'
            });
            xhr.send(data);
            stompClient.send("/chatapp/chatapp.sendMessage", {}, data);
            messageInput.value = '';
        }
    }
    // Gửi yêu cầu HTTP với dữ liệu
}

function onMessageReceived(payload) {
    var message = JSON.parse(payload.body);
    if (message.type === 'JOIN') {
        $("#activeStatus").css("color", "green");
        $("#activeStatus").text("Online");
    } else {

        $("#activeStatus").css("color", "red");
        $("#activeStatus").text("Offline");
    }
    if (message.message != null && message.sender != $('#name').text().trim() && message != window.location.href.split("/")[6]) {
        let newMessage = document.createElement("div");
        newMessage.className = "flex mb-4 cursor-pointer";
        newMessage.innerHTML = `<div class='text-white'>
                                    <div class="text-sm mb-[10px]">${message.sender}</div>
                                    <div class="flex mb-4 cursor-pointer">
                                    <div class="w-9 h-9 rounded-full flex items-center justify-center mr-2">
                                    <img src="https://placehold.co/200x/ffa8e4/ffffff.svg?text=ʕ•́ᴥ•̀ʔ&font=Lato" alt="User Avatar" class="w-8 h-8 rounded-full">
                                    </div>
                                    <div class="flex max-w-96 ${message.color} rounded-lg p-3 gap-3 block-mes">
                                    <p class="message">${message.message}</p>
                                    <p class="text-right">${message.createdAt}</p>
                                                <i class="fas fa-ellipsis-v options-icon"></i>
                                    </div>
                                    </div>
                                </div>`;
        messageArea.appendChild(newMessage);
        messageArea.scrollTop = messageArea.scrollHeight;
    }
    if (message.fileName != null && message.sender != $('#name').text().trim() && message != window.location.href.split("/")[6]) {
        let newMessage = document.createElement("div");
        newMessage.className = "flex mb-4 cursor-pointer";
        newMessage.innerHTML = `<div>
        
                                    <div class="text-white-400 text-sm mb-[10px]">${message.sender}</div>
                                    
                                    <div class="flex mb-4 cursor-pointer">
                                    <div class="w-9 h-9 rounded-full flex items-center justify-center mr-2">
                                    <img src="https://placehold.co/200x/ffa8e4/ffffff.svg?text=ʕ•́ᴥ•̀ʔ&font=Lato" alt="User Avatar" class="w-8 h-8 rounded-full">
                                        
                                    </div>
                                    <div class="flex max-w-96 ${message.color} rounded-lg p-3 gap-3 block-mes">
                                        <i class="fas fa-file-alt text-yellow-500"></i> <!-- Icon file màu vàng -->
                                        <p class="text-white-700 message">${message.fileName}</p>
                                        <i class="fas fa-download text-blue-500 download-icon"
   onclick="downloadFile(
     '${message.fileName}', 
     '${message.fileType}', 
     '${message.fileData}')"></i>

                                        <p class="text-right text-white-500">${message.createdAt}</p>
                                        <i class="fas fa-ellipsis-v text-white-500 options-icon"></i>
                                    </div>
                                    </div>
                                </div>`;
        messageArea.appendChild(newMessage);
        messageArea.scrollTop = messageArea.scrollHeight;
    }
}

