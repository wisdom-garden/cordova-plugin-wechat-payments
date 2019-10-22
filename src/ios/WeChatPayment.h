/********* WeChatPayment.m Cordova Plugin Implementation *******/

#import <Cordova/CDV.h>
#import <Cordova/CDVInvokedUrlCommand.h>
#import "WXApi.h"


@interface WeChatPayment : CDVPlugin <WXApiDelegate>

@property (nonatomic, strong) NSString *currentCallbackId;
@property (nonatomic, strong) NSString *app_id;

- (void)initApp:(CDVInvokedUrlCommand *)command;
- (void)sendPayReq:(CDVInvokedUrlCommand *)command;

@end
