var stompClient = null;
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
        $("#gameplay").show();
        $('#start').prop('disabled', true).hide();
    } else {
        $("#gameplay").hide();
        hideGameControls();
    }
    $("#gameBoard").html("");
}

function connect() {
    clearError();

    $('#username').removeClass('invalid');

    var username = $.trim($('#username').val());

    if (username.length === 0) {
        $('#username').addClass('invalid');
        return;
    }


    var socket = new SockJS('/game-of-three');
    stompClient = Stomp.over(socket);
    stompClient.reconnect_delay = 0;
    stompClient.debug = function (str) {};

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

    }, showError);
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

function getNumberToMakeValueDivisibleByThree(value) {
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

    if (!isAutomatic()) {

        if (gameMessage !== null) {
            if (primaryPlayer && gameMessage.gameStatus === 'START') {
                $('#randomNumberSection').show();
            }

            if (gameMessage.gameStatus === 'PLAY') {
                $('#additionSelector').show();
            }
        }
    } else {
        hideGameControls();
    }
}

function startGameSession() {
    updateGameMode();
    if (primaryPlayer && isAutomatic()) {
        delay(function () {
            var randomNumber = generateRandomNumber();
            showMessage('You generated the random number: ' + randomNumber);
            sendRandomNumber(randomNumber);
        });
    }

}

function processGameMessage() {
    if (null === gameMessage) {
        return;
    }
    clearError();
    hideGameControls();

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
        value: randomNumber
    };
    stompClient.send('/app/game.number', {}, JSON.stringify(message));
    gameMessage = null;
}

function makeMove() {
    var value = gameMessage.value;
    gameValue = value;
    showMessage(opponent + ' sent value ' + value);
    if (isAutomatic()) {
        delay(function () {
            var addition = getNumberToMakeValueDivisibleByThree(value);
            sendMove(value, addition);
            gameMessage = null;
        });
    } else {
        $('#additionSelector').show();
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
        $('#start').prop('disabled', false).html('<i class="fas fa-sync"></i> Rematch').show();
    }

    hideGameControls();
    gameMessage = null;
}

function coplayerDisconnected() {
    showMessage(gameMessage.content);
    primaryPlayer = true;
    $('#start').prop('disabled', false).html('<i class="far fa-play-circle"></i> New Game').show();
    $('#opponentLabel').html('').hide();

    hideGameControls();
    gameMessage = null;
}

function delay(fn) {
    setTimeout(fn, 1000);
}

function addNumber() {
    var selectedNumber = $('#addSelect option:selected').val();
    console.log(selectedNumber);
    var result = parseInt(gameValue) + parseInt(selectedNumber);

    if (result % 3 != 0) {
        showError('Addition should return a number divisible by 3');
        return;
    }

    sendMove(gameValue, parseInt(selectedNumber));
    hideGameControls();
}

function retrieveRandomNumber() {
    $('#randomNumber').removeClass('invalid');
    var randomNumber = parseInt($('#randomNumber').val());
    if (isNaN(randomNumber) || randomNumber <= 0) {
        $('#randomNumber').addClass('invalid');
        return;
    }
    sendRandomNumber(randomNumber);
    hideGameControls();
}

function isAutomatic() {
    return $('#mode option:selected').text() === 'AUTOMATIC';
}

function hideGameControls() {
    $('#randomNumberSection').hide();
    $('#additionSelector').hide();
}

function clearError() {
    $('#errorMessage').hide();
}

function showError(error) {
    console.log('CALLED **');
    console.log('Error: ', error);
    var message = null;

    if (error.headers && error.headers.message) {
        message = error.headers.message;
    } else {
        message = error;
    }

    $('#errorMessage span').html(message);
    $('#errorMessage').show();
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