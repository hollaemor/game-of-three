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
    $('#start').prop('disabled', !connected);
    $('#username').prop('disabled', connected);

    if (connected) {
        $("#conversation").show();
        $('#start').prop('disabled', true).hide();
    } else {
        $("#conversation").hide();
    }
    $("#gameBoard").html("");
}

function connect() {
    var socket = new SockJS('/game-of-three');
    stompClient = Stomp.over(socket);
    var username = $('#username').val();
    stompClient.connect({
        username: username
    }, function (frame) {
        setConnected(true);

        start();
        stompClient.subscribe('/user/queue/updates', function (response) {
            gameMessage = JSON.parse(response.body);
            processGameMessage();
        });

        stompClient.subscribe('/user/queue/errors', function (response) {
            console.log(response.body);
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
    primaryPlayer = false;
}

function showMessage(message) {
    $("#gameBoard").prepend('<tr><td colspan="2">' + message + '</td></tr>');
}

function start() {
    $('#gameBoard').html('');
    stompClient.send('/app/game.start', {});
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

    isAutomatic = $('#mode option:selected').text() === 'AUTOMATIC';
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
    updateGameMode();
    if (primaryPlayer) {
        if (isAutomatic) {
            delay(function () {
                var randomNumber = generateRandomNumber();
                showMessage('You generated the random number: ' + randomNumber);
                sendRandomNumber(randomNumber);
            });
        }
    }
}

function processGameMessage() {
    if (null === gameMessage) {
        return;
    }
    switch (gameMessage.gameStatus) {
        case 'WAITING':
            showMessage(gameMessage.content);
            gameMessage = null;
            break;
        case 'START':
            $('#gameBoard').html('');
            $('#opponentLabel').html('[Opponent: ' + gameMessage.opponent + ']').show();
            opponent = gameMessage.opponent;
            primaryPlayer = gameMessage.primaryPlayer;
            startGameSession();
            //todo: show other form fields depending on which kind of player and game mode
            break;
        case 'PLAY':
            makeMove();
            break;
        case 'GAMEOVER':
            gameOver();
            break;
        case 'DISCONNECT':
            coplayerDisconnected();
            break;
    }
}

function generateRandomNumber() {
    return Math.floor(Math.random() * 100) + 1;
}

function sendRandomNumber(randomNumber) {
    var message = {
        coplayer: opponent,
        type: 'PLAY',
        value: randomNumber
    };
    console.log(message);
    stompClient.send('/app/game.number', {}, JSON.stringify(message));
    gameMessage = null;
}

function makeMove() {
    var value = gameMessage.value;
    gameValue = value;
    showMessage(opponent + ' sent value ' + value);
    if (isAutomatic) {
        delay(function () {
            var addition = getAddition(value);
            sendMove(value, addition);
            gameMessage = null;
        });
    }
}

function sendMove(value, move) {
    showMessage('You added ' + move + ' to ' + value + ' to make it divisible by 3');
    var updatedValue = value + move;
    showMessage(updatedValue + ' divided by 3 = ' + updatedValue / 3);
    var message = {
        value: value,
        move: move
    };

    stompClient.send('/app/game.play', {}, JSON.stringify(message));
    gameMessage = null;
}

function gameOver() {
    var message = gameMessage.winner ? 'You won the game :)' : 'You lost the game :(';
    showMessage(message);

    if (primaryPlayer) {
        $('#start').prop('disabled', false).show();
    }

    gameMessage = null;
}

function coplayerDisconnected() {
    showMessage(gameMessage.content);
    primaryPlayer = true;
    $('#start').prop('disabled', false).show();
    $('#opponentLabel').html('').hide();

    gameMessage = null;
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
    setConnected(false);

    $("form").on('submit', function (e) {
        e.preventDefault();
    });

    $("#connect").click(connect);
    $("#disconnect").click(disconnect);
    $('#start').click(function () {
        start();
        $(this).prop('disabled', true);
    });
    $('#mode').change(function () {
        updateGameMode();
        processGameMessage();
    }).change();

    $('#btnNumberAdder').click(addNumber);
    $('#btnRandomNumber').click(retrieveRandomNumber);
});