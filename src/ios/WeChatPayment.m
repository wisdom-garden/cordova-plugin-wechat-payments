/********* WeChatPayment.m Cordova Plugin Implementation *******/

#import "WeChatPayment.h"
#import "ApiXml.h"
#import <Cordova/CDV.h>
#import <Cordova/CDVInvokedUrlCommand.h>

@implementation WeChatPayment
#pragma mark "API"


-(void)pluginInitialize {}


-(void) prepareForExec:(CDVInvokedUrlCommand *)command{
    self.currentCallbackId = command.callbackId;
}


-(void) endForExec{
    self.currentCallbackId = nil;
}


-(NSDictionary *)checkArgs:(CDVInvokedUrlCommand *)command{
    // check arguments
    NSDictionary *params = [command.arguments objectAtIndex:0];
    if (!params) {
        [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"参数错误"] callbackId:command.callbackId];

        [self endForExec];
        return nil;
    }
    return params;
}


- (void)initApp:(CDVInvokedUrlCommand *)command{
    [self prepareForExec:command];
    NSDictionary *params = [self checkArgs:command];
    if (params == nil) {
        return;
    }
    NSString *appId = params[@"appId"];
    if (appId){
        self.app_id = appId;
        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [self.commandDelegate sendPluginResult:result callbackId:self.currentCallbackId];
        return;
    }
};


- (void)sendPayReq:(CDVInvokedUrlCommand *)command{
    [self prepareForExec:command];
    NSDictionary *prepayWeXinObj = [self checkArgs:command];
    if(prepayWeXinObj == nil){
        return;
    }
    NSString *appId = prepayWeXinObj[@"appid"];
    NSString *prepayId = prepayWeXinObj[@"prepayid"];
    NSString *partnerId = prepayWeXinObj[@"partnerid"];
    NSString *package = prepayWeXinObj[@"package"];
    NSString *nonceStr = prepayWeXinObj[@"noncestr"];
    NSString *timeStamp = prepayWeXinObj[@"timestamp"];
    NSString *sign = prepayWeXinObj[@"sign"];

    [WXApi registerApp:appId];
    // 获取预支付订单id，调用微信支付sdk
    if (prepayId){

        // 调起微信支付
        PayReq *request   = [[PayReq alloc] init];
        request.prepayId  = prepayId;
        request.partnerId = partnerId;
        request.package   = package;
        request.nonceStr = nonceStr;
        request.timeStamp = [timeStamp intValue];
        request.sign = sign;

        // 在支付之前，如果应用没有注册到微信，应该先调用 [WXApi registerApp:appId] 将应用注册到微信
        [WXApi sendReq:request];
    }
};


- (void)onResp:(BaseResp *)resp{
    CDVPluginResult *result = nil;
    BOOL success = NO;
    if([resp isKindOfClass:[SendMessageToWXResp class]]){
        switch (resp.errCode)
        {
            case WXSuccess:
                result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
                success = YES;
            break;

            case WXErrCodeCommon:
                result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"普通错误类型"];
            break;

            case WXErrCodeUserCancel:
                result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"用户点击取消并返回"];
            break;

            case WXErrCodeSentFail:
                result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"发送失败"];
            break;

            case WXErrCodeAuthDeny:
                result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"授权失败"];
            break;

            case WXErrCodeUnsupport:
                result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"微信不支持"];
            break;
        }
        if (!result)
        {
            result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Unknown"];
        }
        [self.commandDelegate sendPluginResult:result callbackId:self.currentCallbackId];
    } else if ([resp isKindOfClass:[PayResp class]]) {
        PayResp *response = (PayResp *)resp;
        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:[NSString stringWithFormat:@"%d",response.errCode]];
        [self.commandDelegate sendPluginResult:result callbackId:[self currentCallbackId]];
    }
    [self endForExec];
}


#pragma mark "CDVPlugin Overrides"
- (void)handleOpenURL:(NSNotification *)notification{
    NSURL* url = [notification object];
    if ([url isKindOfClass:[NSURL class]] && [url.scheme isEqualToString:self.app_id])
    {
        [WXApi handleOpenURL:url delegate:self];
    }
}

@end
