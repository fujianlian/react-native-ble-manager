//
//  AppLog.h
//  BleSampleOmron
//
//  Copyright © 2016 Omron Healthcare Co., Ltd. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <pthread.h>

#define AppLogD(format, ...) [AppLog D:[NSString stringWithFormat:format, ##__VA_ARGS__] function:(char *)__PRETTY_FUNCTION__ line:__LINE__ tid:pthread_mach_thread_np(pthread_self())]
#define AppLogI(format, ...) [AppLog I:[NSString stringWithFormat:format, ##__VA_ARGS__] function:(char *)__PRETTY_FUNCTION__ line:__LINE__ tid:pthread_mach_thread_np(pthread_self())]
#define AppLogW(format, ...) [AppLog W:[NSString stringWithFormat:format, ##__VA_ARGS__] function:(char *)__PRETTY_FUNCTION__ line:__LINE__ tid:pthread_mach_thread_np(pthread_self())]
#define AppLogE(format, ...) [AppLog E:[NSString stringWithFormat:format, ##__VA_ARGS__] function:(char *)__PRETTY_FUNCTION__ line:__LINE__ tid:pthread_mach_thread_np(pthread_self())]
#define AppLogDMethodIn(format, ...) [AppLog DMethodIn:[NSString stringWithFormat:format, ##__VA_ARGS__] function:(char *)__PRETTY_FUNCTION__ line:__LINE__ tid:pthread_mach_thread_np(pthread_self())]
#define AppLogDMethodOut(format, ...) [AppLog DMethodOut:[NSString stringWithFormat:format, ##__VA_ARGS__] function:(char *)__PRETTY_FUNCTION__ line:__LINE__ tid:pthread_mach_thread_np(pthread_self())]
#define AppLogBleInfo(format, ...) [AppLog BleInfo:[NSString stringWithFormat:format, ##__VA_ARGS__] tid:pthread_mach_thread_np(pthread_self())]

typedef NS_ENUM(NSUInteger, LogLevel) {
    LogLevelVerbose,
    LogLevelDebug,
    LogLevelInfo,
    LogLevelWarn,
    LogLevelError,
    NumOfLogLevels,
};

@interface LogItem : NSObject

@property (nonatomic, readonly) LogLevel level;
@property (strong, nonatomic, readonly) NSString *log;

- (id)initWithLevel:(LogLevel)level log:(NSString *)log;

@end

@interface AppLog : NSObject

+ (NSString *)LogLevelName:(LogLevel)level;

+ (void)SetLogLevel:(LogLevel)level;
+ (LogLevel)GetLogLevel;

+ (void)D:(NSString *)message function:(char *)function line:(int)line tid:(int)tid;
+ (void)I:(NSString *)message function:(char *)function line:(int)line tid:(int)tid;
+ (void)W:(NSString *)message function:(char *)function line:(int)line tid:(int)tid;
+ (void)E:(NSString *)message function:(char *)function line:(int)line tid:(int)tid;
+ (void)DMethodIn:(NSString *)message function:(char *)function line:(int)line tid:(int)tid;
+ (void)DMethodOut:(NSString *)message function:(char *)function line:(int)line tid:(int)tid;
+ (void)BleInfo:(NSString *)message tid:(int)tid;
+ (void)OutputLog:(NSString *)message level:(LogLevel)level tag:(NSString *)tag function:(char *)function line:(int)line tid:(int)tid;

@end
