//
//  AppLog.m
//  BleSampleOmron
//
//  Copyright © 2016 Omron Healthcare Co., Ltd. All rights reserved.
//

#import "AppLog.h"
//#import "LogViewService.h"

static const BOOL OUTPUT_LOG_MODE = YES;
static NSString *const BLE_LOG_TAG = @"BLE_LOG";
static NSString *const LOG_PREFIX_DEBUG = @"[DEBUG] ";
static NSString *const LOG_PREFIX_INFO = @"[INFO]  ";
static NSString *const LOG_PREFIX_WARN = @"[WARN]  ";
static NSString *const LOG_PREFIX_ERROR = @"[ERROR] ";
static NSString *const LOG_PREFIX_METHOD_IN = @"[IN]    ";
static NSString *const LOG_PREFIX_METHOD_OUT = @"[OUT]   ";
static NSString *const LOG_PREFIX_BLE_INFO  = @"[LOG_OUT]";
static NSString *TAG = @"BleSampleOmron";

@implementation LogItem

- (id)initWithLevel:(LogLevel)level log:(NSString *)log {
    self = [super init];
    if (self) {
        _level = level;
        _log   = log;
    }
    return self;
}

@end

@interface AppLog ()

@property (atomic) LogLevel outputLogLevel;

@end

@implementation AppLog

static AppLog *_instance = nil;

+ (AppLog *)sharedInstance {
    @synchronized (self) {
        if (!_instance) {
            _instance = [AppLog new];
        }
        return _instance;
    }
}

+ (NSString *)LogLevelName:(LogLevel)level {
    switch (level) {
        case LogLevelVerbose:
            return @"Verbose";
        case LogLevelDebug:
            return @"Debug";
        case LogLevelInfo:
            return @"Info";
        case LogLevelWarn:
            return @"Warning";
        case LogLevelError:
            return @"Error";
            
        default:
            return nil;
    }
}


+ (void)SetLogLevel:(LogLevel)level {
    [AppLog sharedInstance].outputLogLevel = level;
}

+ (LogLevel)GetLogLevel {
    return [AppLog sharedInstance].outputLogLevel;
}


+ (void)D:(NSString *)message function:(char *)function line:(int)line tid:(int)tid {
    [[self sharedInstance] d:message function:function line:line tid:tid];
}

+ (void)I:(NSString *)message function:(char *)function line:(int)line tid:(int)tid {
    [[self sharedInstance] i:message function:function line:line tid:tid];
}

+ (void)W:(NSString *)message function:(char *)function line:(int)line tid:(int)tid {
    [[self sharedInstance] w:message function:function line:line tid:tid];
}

+ (void)E:(NSString *)message function:(char *)function line:(int)line tid:(int)tid {
    [[self sharedInstance] e:message function:function line:line tid:tid];
}

+ (void)DMethodIn:(NSString *)message function:(char *)function line:(int)line tid:(int)tid {
    [[self sharedInstance] dMethodIn:message function:function line:line tid:tid];
}

+ (void)DMethodOut:(NSString *)message function:(char *)function line:(int)line tid:(int)tid {
    [[self sharedInstance] dMethodOut:message function:function line:line tid:tid];
}

+ (void)BleInfo:(NSString *)message tid:(int)tid {
    [[self sharedInstance] bleInfo:message tid:tid];
}

+ (void)OutputLog:(NSString *)message level:(LogLevel)level tag:(NSString *)tag function:(char *)function line:(int)line tid:(int)tid {
    [[self sharedInstance] outputLog:message level:level tag:tag function:function line:line tid:tid];
}


- (id)init {
    self = [super init];
    if (self) {
        self.outputLogLevel = LogLevelVerbose;
    }
    return self;
}

- (void)d:(NSString *)message function:(char *)function line:(int)line tid:(int)tid {
    NSString *messageWithPrefix = [LOG_PREFIX_DEBUG stringByAppendingString:message];
    [self outputLog:messageWithPrefix level:LogLevelDebug tag:TAG function:function line:line tid:tid];
}

- (void)i:(NSString *)message function:(char *)function line:(int)line tid:(int)tid {
    NSString *messageWithPrefix = [LOG_PREFIX_INFO stringByAppendingString:message];
    [self outputLog:messageWithPrefix level:LogLevelInfo tag:TAG function:function line:line tid:tid];
}

- (void)w:(NSString *)message function:(char *)function line:(int)line tid:(int)tid {
    NSString *messageWithPrefix = [LOG_PREFIX_WARN stringByAppendingString:message];
    [self outputLog:messageWithPrefix level:LogLevelWarn tag:TAG function:function line:line tid:tid];
}

- (void)e:(NSString *)message function:(char *)function line:(int)line tid:(int)tid {
    NSString *messageWithPrefix = [LOG_PREFIX_ERROR stringByAppendingString:message];
    [self outputLog:messageWithPrefix level:LogLevelError tag:TAG function:function line:line tid:tid];
}

- (void)dMethodIn:(NSString *)message function:(char *)function line:(int)line tid:(int)tid {
    NSString *messageWithPrefix = [LOG_PREFIX_METHOD_IN stringByAppendingString:message];
    [self outputLog:messageWithPrefix level:LogLevelDebug tag:TAG function:function line:line tid:tid];
}

- (void)dMethodOut:(NSString *)message function:(char *)function line:(int)line tid:(int)tid {
    NSString *messageWithPrefix = [LOG_PREFIX_METHOD_OUT stringByAppendingString:message];
    [self outputLog:messageWithPrefix level:LogLevelDebug tag:TAG function:function line:line tid:tid];
}

- (void)bleInfo:(NSString *)message tid:(int)tid {
    NSString *messageWithPrefix = [LOG_PREFIX_BLE_INFO stringByAppendingString:message];
    [self outputLog:messageWithPrefix level:LogLevelInfo tag:BLE_LOG_TAG function:"" line:-1 tid:tid];
}

- (void)outputLog:(NSString *)message level:(LogLevel)level tag:(NSString *)tag function:(char *)function line:(int)line tid:(int)tid {
    if (!OUTPUT_LOG_MODE) {
        return;
    }
    
    NSString *formattedLog = [self createFormattedLog:message level:level tag:tag function:function line:line tid:tid];
    LogItem *item = [[LogItem alloc] initWithLevel:level log:formattedLog];
//    [LogViewService AddLog:item];
#if DEBUG
    NSLog(@"%@", formattedLog);
#endif
}

- (NSString *)createFormattedLog:(NSString *)message level:(LogLevel)level tag:(NSString *)tag function:(char *)function line:(int)line tid:(int)tid {
    NSMutableString *formattedLog = [NSMutableString string];
    
    NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
    formatter.dateFormat = @"MM-dd HH:mm:ss.SSS";
    [formattedLog appendString:[formatter stringFromDate:[NSDate date]]];
    
    NSString *c = nil;
    switch (level) {
        case LogLevelDebug:
            c = @"D";
            break;
        case LogLevelInfo:
            c = @"I";
            break;
        case LogLevelWarn:
            c = @"W";
            break;
        case LogLevelError:
            c = @"E";
            break;
            
        default:
            c = @"V";
            break;
    }
    if (strlen(function) == 0 && line < 0) {
        [formattedLog appendFormat:@" %@/%@( %d): ", c, tag, tid];
    } else {
        [formattedLog appendFormat:@" %@/%@( %d): %s:%d ", c, tag, tid, function, line];
    }
    
    [formattedLog appendString:message];
    
    return formattedLog;
}

@end
