var stompClient = null;
var isAutomatic = false;
var opponent = null;
var primaryPlayer = false;
var gameStarted = false;
var gameValue = 0;
var gameMessage = null;

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    if (connected) {
        $("#conversation").show();
    } else {
        $("#conversation").hide();
    }
    $("#greetings").html("");
}

function connect() {
    var socket = new SockJS('/game-of-three');
    stompClient = Stomp.over(socket);
    var username = $('#username').val();
    stompClient.connect({
        username: username
    }, function (frame) {
        setConnected(true);
        console.log('Connected: ' + frame);
        stompClient.subscribe('/topic/greetings', function (greeting) {
            showGreeting(JSON.parse(greeting.body).content);
        });

        stompClient.subscribe('/user/queue/updates', function (response) {
            gameMessage = JSON.parse(response.body);
            processGameMessage();
        });

    }, function (error) {
        console.log(error);
        console.log(error.headers.message);
    });
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
}

function showGreeting(message) {
    $("#greetings").prepend("<tr><td>" + message + "</td></tr>");
}

function start() {
    stompClient.send('/app/game.start', {});
}

function play(value, addition) {
    if ((value + addition) % 3 != 0) {
        //todo: show error message
    }
    var message = {
        type: 'PLAY',
        coplayer: opponent,
        value: value,
        play: play
    };

    stompClient.send('/app/game.play', {}, message);
}

//todo: change function name
function getAddition(value) {
    var modulo = value % 3;
    switch (modulo) {
        case 0:
            return 0;
        case 1:
            return -1;
        default:
            return 1;
    }
}

function updateGameMode() {
    if (!gameStarted) {
        return;
    }
    isAutomatic = $('option:selected', this).text() === 'AUTOMATIC';
    if (!isAutomatic) {
        if (primaryPlayer) {
            $('#randomNumberSection').show();
        }
        $('#additionSelector').show();
    } else {
        $('#randomNumberSection').hide();
        $('#additionSelector').hide();
    }
}

function startGameSession() {
    gameStarted = true;
    $('#mode').change();
    if (primaryPlayer) {
        if (isAutomatic) {
            delay(function () {
                var randomNumber = generateRandomNumber();
                showGreeting('You generated the random number: ' + randomNumber);
                sendRandomNumber(randomNumber);
            });

        }
    }
}

function processGameMessage() {
    if (null == gameMessage) {
        return;
    }
    switch (gameMessage.gameStatus) {
        case 'WAITING':
            showGreeting(gameMessage.content);
            break;
        case 'START':
            showGreeting("You're connected to " + gameMessage.opponent);
            opponent = gameMessage.opponent;
            primaryPlayer = gameMessage.primaryPlayer;
            startGameSession();
            //todo: show other form fields depending on which kind of player and game mode
            break;
        case 'PLAY':
            makeMove(gameMessage);
            break;
        case 'GAMEOVER':
            gameOver(gameMessage);
            break;
        case 'DISCONNECT':
            coplayerDisconnected(gameMessage);
            break;
    }
    gameMessage = null;
}

function generateRandomNumber() {
    return Math.floor(Math.random() * 100);
}

function sendRandomNumber(randomNumber) {
    var message = {
        coplayer: opponent,
        type: 'PLAY',
        value: randomNumber
    };
    console.log(message);
    stompClient.send('/app/game.number', {}, JSON.stringify(message));
}

function makeMove(message) {
    var value = message.value;
    gameValue = value;
    showGreeting(opponent + ' sent value ' + value);
    if (isAutomatic) {
        delay(function () {
            var addition = getAddition(value);
            sendMove(value, addition);
        });
    }
}

function sendMove(value, move) {
    showGreeting('You added ' + move + ' to ' + value + ' to make it divisible by 3');
    var updatedValue = value + move;
    showGreeting(updatedValue + ' divided by 3 = ' + updatedValue / 3);
    var message = {
        value: value,
        move: move
    };

    stompClient.send('/app/game.play', {}, JSON.stringify(message));
}

function gameOver(message) {
    var display = message.winner ? 'You won the game :)' : 'You lost the game :(';
    showGreeting(display);
}

function coplayerDisconnected(message) {
    showGreeting(message.content);
}

function delay(fn) {
    setTimeout(fn, 1000);
}

function addNumber() {
    var selectedNumber = $('#addSelect option:selected').val();
    console.log(selectedNumber);
    var result = parseInt(gameValue) + parseInt(selectedNumber);
    console.log(result);

    if (result % 3 != 0) {
        console.error('Addition should return a number divisible by 3');
        //todo: display error message
        return;
    }

    sendMove(gameValue, parseInt(selectedNumber));
}

function retrieveRandomNumber() {
    var randomNumber = parseInt($('#randomNumber').val());
    sendRandomNumber(randomNumber);
}

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $("#connect").click(function () {
        connect();
    });
    $("#disconnect").click(function () {
        disconnect();
    });
    $('#start').click(function () {
        start()
    });
    $('#mode').change(updateGameMode).change();

    $('#btnNumberAdder').click(addNumber);
    $('#btnRandomNumber').click(retrieveRandomNumber);
});