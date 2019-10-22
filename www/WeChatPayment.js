let exec = require('cordova/exec');

module.exports = {
    initApp: function(appId, onSuccess, onError){
        exec(onSuccess, onError, 'WeChatPayment', 'initApp', [{'appId' : appId}]);
    },

    sendPayReq: function(prepayWeXinObj, onSuccess, onError) {
        exec(onSuccess, onError, 'WeChatPayment', 'sendPayReq', [prepayWeXinObj]);
    }
};