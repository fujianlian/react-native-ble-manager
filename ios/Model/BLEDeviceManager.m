//
//  BLEDeviceManager.m
//  BleSampleOmron
//
//  Copyright © 2016 Omron Healthcare Co., Ltd. All rights reserved.
//

#import "BLEDeviceManager.h"
#import <UIKit/UIKit.h>
#import <CoreBluetooth/CoreBluetooth.h>
#import "BleManager.h"
///---------------------------------------------------------------------------------------
#pragma mark - Private defines for BLE
///---------------------------------------------------------------------------------------

// Service UUID String.
static NSString * const CurrentTimeServiceUUIDString = @"1805";
static NSString * const BatteryServiceUUIDString = @"180F";
static NSString * const BloodPressureServiceUUIDString = @"1810";
static NSString * const WeightScaleServiceUUIDString = @"181D";

// Characteristic UUID String.
static NSString * const CurrentTimeCharacteristicUUIDString = @"2A2B";
static NSString * const BatteryLevelCharacteristicUUIDString = @"2A19";
static NSString * const BloodPressureMeasurementCharacteristicUUIDString = @"2A35";
static NSString * const BloodPressureFeatureCharacteristicUUIDString = @"2A49";
static NSString * const WeightMeasurementCharacteristicUUIDString = @"2A9D";
static NSString * const WeightFeatureCharacteristicUUIDString = @"2A9E";

// Blood pressure measurement characteristic Flags
typedef NS_OPTIONS(UInt8, BloodPressureMeasurementFlag) {
    BloodPressureMeasurementFlagKpaUnit = 1 << 0,
    BloodPressureMeasurementFlagTimeStampPresent = 1 << 1,
    BloodPressureMeasurementFlagPulseRatePresent = 1 << 2,
    BloodPressureMeasurementFlagUserIDPresent = 1 << 3,
    BloodPressureMeasurementFlagStatusPresent = 1 << 4,
};

// Weight measurement characteristic Flags
typedef NS_OPTIONS(UInt8, WeightMeasurementFlags) {
    WeightMeasurementFlagImperialUnit = 1 << 0,
    WeightMeasurementFlagTimeStampPresent = 1 << 1,
    WeightMeasurementFlagUserIDPresent = 1 << 2,
    WeightMeasurementFlagBMIAndHeightPresent = 1 << 3,
};

#pragma pack(1) // structure 1byte alignment begin

typedef struct {
    UInt16 year;
    UInt8 month;
    UInt8 day;
    UInt8 hours;
    UInt8 minutes;
    UInt8 seconds;
} DateTime;

typedef struct {
    DateTime dateTime;
    UInt8 dayOfWeek;
    UInt8 fractions256;
    UInt8 adjustReason;
} CurrentTime;

#pragma pack() // structure 1byte alignment end

NSDate* ble_device_convert_to_nsdate(DateTime dateTime) {
    NSDateComponents *components = [[NSDateComponents alloc] init];
    components.year = dateTime.year;
    components.month = dateTime.month;
    components.day = dateTime.day;
    components.hour = dateTime.hours;
    components.minute = dateTime.minutes;
    components.second = dateTime.seconds;
    return [[NSCalendar currentCalendar] dateFromComponents:components];
}

typedef UInt16 SFloat;

Float32 ble_device_convert_to_float32(SFloat sfloat) {
    SInt8 exponent = (SInt8)(sfloat >> 12);
    SInt16 mantissa = (SInt16)(sfloat & 0x0FFF);
    return (Float32)(mantissa * pow(10, exponent));
}

///---------------------------------------------------------------------------------------
#pragma mark - Private defines for dispatch
///---------------------------------------------------------------------------------------

// GCD Queue label.
static const char *CallbackQueueLabel = "BLEDeviceManager-callback";
static const char *InternalQueueLabel = "BLEDeviceManager-internal";

dispatch_queue_t ble_device_dispatch_get_callback_queue() {
    static dispatch_queue_t callbackQueue = NULL;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
#ifdef BLE_DEVICE_OPTION_CALLBACK_USING_MAIN_QUEUE
        NSLog(@"Unused %s queue", CallbackQueueLabel);
        callbackQueue = dispatch_get_main_queue();
#else // BLE_DEVICE_OPTION_CALLBACK_USING_MAIN_QUEUE
        callbackQueue = dispatch_queue_create(CallbackQueueLabel, DISPATCH_QUEUE_SERIAL);
#endif // BLE_DEVICE_OPTION_CALLBACK_USING_MAIN_QUEUE
    });
    return callbackQueue;
}

BOOL ble_device_dispatch_current_queue_is_callback_queue() {
    BOOL ret = NO;
#ifdef BLE_DEVICE_OPTION_CALLBACK_USING_MAIN_QUEUE
    ret = [NSThread isMainThread];
#else // BLE_DEVICE_OPTION_CALLBACK_USING_MAIN_QUEUE
    const char *currentQueueLabel = dispatch_queue_get_label(DISPATCH_CURRENT_QUEUE_LABEL);
    ret = (strcmp(CallbackQueueLabel, currentQueueLabel) == 0);
#endif // BLE_DEVICE_OPTION_CALLBACK_USING_MAIN_QUEUE
    return ret;
}

void ble_device_dispatch_to_callback_queue(dispatch_block_t block) {
    if (block) {
        if (ble_device_dispatch_current_queue_is_callback_queue()) {
            block();
        }
        else {
            dispatch_async(ble_device_dispatch_get_callback_queue(), block);
        }
    }
}

dispatch_queue_t ble_device_dispatch_get_internal_queue() {
    static dispatch_queue_t internalQueue = NULL;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        internalQueue = dispatch_queue_create(InternalQueueLabel, DISPATCH_QUEUE_SERIAL);
    });
    return internalQueue;
}

BOOL ble_device_dispatch_current_queue_is_internal_queue() {
    const char *currentQueueLabel = dispatch_queue_get_label(DISPATCH_CURRENT_QUEUE_LABEL);
    return (strcmp(InternalQueueLabel, currentQueueLabel) == 0);
}

void ble_device_dispatch_to_internal_queue(dispatch_block_t block) {
    if (block) {
        if (ble_device_dispatch_current_queue_is_internal_queue()) {
            block();
        }
        else {
            dispatch_async(ble_device_dispatch_get_internal_queue(), block);
        }
    }
}

///---------------------------------------------------------------------------------------
#pragma mark - BLEDevice class
///---------------------------------------------------------------------------------------

@interface BLEDevice : NSObject {
    @protected
    CBPeripheral *_peripheral;
    NSDictionary<NSString *, id> *_advertisementData;
    NSString *_localName;
    NSNumber *_RSSI;
}
- (instancetype)initWithPeripheral:(CBPeripheral *)peripheral advertisementData:(NSDictionary<NSString *, id> *)advertisementData RSSI:(NSNumber *)RSSI;
- (void)updateWithAdvertisementData:(NSDictionary<NSString *, id> *)advertisementData RSSI:(NSNumber *)RSSI;
@property (readonly) NSUUID *identifier;
@property (readonly) CBPeripheral *peripheral;
@property (readonly) NSDictionary<NSString *, id> *advertisementData;
@property (readonly) NSString *localName;
@property (readonly) NSString *modelName;
@property (readonly) NSNumber *RSSI;
@property (readonly) NSDictionary<NSString *, id> *deviceInfo;
@end

@implementation BLEDevice
- (instancetype)initWithPeripheral:(CBPeripheral *)peripheral advertisementData:(NSDictionary<NSString *, id> *)advertisementData RSSI:(NSNumber *)RSSI {
    NSString *localName = advertisementData[CBAdvertisementDataLocalNameKey];
    if (!peripheral || !localName || !RSSI) {
        return nil;
    }
    self = [super init];
    if (self) {
        _peripheral = peripheral;
        _advertisementData = advertisementData;
        _localName = advertisementData[CBAdvertisementDataLocalNameKey];
        _RSSI = RSSI;
    }
    return self;
}
- (BOOL)isEqual:(id)object {
    BOOL ret = NO;
    if ([object isKindOfClass:[self class]]) {
        typeof(self) other = object;
        ret = [self.peripheral isEqual:other.peripheral];
    }
    return ret;
}
- (NSUInteger)hash {
    return self.peripheral.hash;
}
- (NSString *)description {
    NSString *superDescription = [super description];
    return [NSString stringWithFormat:@"%@[%@(%@)]", superDescription, self.modelName, self.localName];
}
- (void)updateWithAdvertisementData:(NSDictionary<NSString *, id> *)advertisementData RSSI:(NSNumber *)RSSI {
    if (advertisementData && ![self.advertisementData isEqualToDictionary:advertisementData]) {
        _advertisementData = advertisementData;
    }
    NSString *localName = advertisementData[CBAdvertisementDataLocalNameKey];
    if (localName && self.localName != localName) {
        _localName = localName;
    }
    _RSSI = RSSI;
}
- (NSString *)modelName {
    return self.peripheral.name;
}
- (NSUUID *)identifier {
    return self.peripheral.identifier;
}
- (NSDictionary<NSString *, id> *)deviceInfo {
    if (!self.localName) {
        return nil;
    }
    return @{
             BLEDeviceInfoAdvertisementDataKey: self.advertisementData,
             BLEDeviceInfoIdentifierKey: self.peripheral.identifier,
             BLEDeviceInfoLocalNameKey: self.localName,
             BLEDeviceInfoModelNameKey: self.modelName,
             BLEDeviceInfoRSSIKey: self.RSSI,
             };
}
@end

///---------------------------------------------------------------------------------------
#pragma mark - BLEDeviceDataReadingCallbackBundle class
///---------------------------------------------------------------------------------------

@interface BLEDeviceDataReadingCallbackBundle : NSObject
@property BLEDeviceDataObserverBlock dataObserverBlock;
@property BLEDeviceConnectionObserverBlock connectionObserverBlock;
@property BLEDeviceCompletionBlock completionBlock;
@property NSInteger retryCount;
@end

@implementation BLEDeviceDataReadingCallbackBundle
- (instancetype)initWithDataObserver:(BLEDeviceDataObserverBlock)dataObserver connectionObserver:(BLEDeviceConnectionObserverBlock)connectionObserver completion:(BLEDeviceCompletionBlock)completion {
    if (self) {
        _dataObserverBlock = dataObserver;
        _connectionObserverBlock = connectionObserver;
        _completionBlock = completion;
        _retryCount = 0;
    }
    return self;
}
@end

///---------------------------------------------------------------------------------------
#pragma mark - BLEDeviceManager Class Extension
///---------------------------------------------------------------------------------------

@interface BLEDeviceManager () <CBCentralManagerDelegate, CBPeripheralDelegate>

@property CBCentralManager *central;
@property (nonatomic, assign, readwrite) BLEDeviceManagerState state;

@property NSMutableDictionary<NSUUID *, BLEDevice *> *scanCacheDictionary;
@property dispatch_block_t scanStartBlock;
@property BLEDeviceScanObserverBlock scanObserverBlock;
@property BLEDeviceCompletionBlock scanCompletionBlock;

@property NSMutableDictionary<CBPeripheral *, BLEDeviceDataReadingCallbackBundle *> *deviceDataReadingCallbackBundles;

@property CBUUID *bloodPressureServiceUUID;
@property CBUUID *batteryServiceUUID;
@property CBUUID *currentTimeServiceUUID;
@property CBUUID *weightScaleServiceUUID;
@property CBUUID *bloodPressureMeasurementCharacteristicUUID;
@property CBUUID *bloodPressureFeatureCharacteristicUUID;
@property CBUUID *batteryLevelCharacteristicUUID;
@property CBUUID *currentTimeCharacteristicUUID;
@property CBUUID *weightMeasurementCharacteristicUUID;
@property CBUUID *weightFeatureCharacteristicUUID;

@end

///---------------------------------------------------------------------------------------
#pragma mark - LogProxy class
///---------------------------------------------------------------------------------------

@interface LogProxy : NSProxy
@property (nonatomic, strong) NSObject *object;
- (instancetype)initWithObject:(NSObject *)object;
@end

@implementation LogProxy
- (instancetype)initWithObject:(NSObject *)object {
    _object = object;
    return (_object ? self : nil);
}
- (void)forwardInvocation:(NSInvocation *)invocation {
    if (_object) {
        invocation.target = _object;
        [invocation invoke];
    }
}
- (NSMethodSignature *)methodSignatureForSelector:(SEL)sel {
    if (_object) {
        NSLog(@"[%@]%@", [_object class], NSStringFromSelector(sel));
    }
    return (_object ? [_object methodSignatureForSelector:sel] : [super methodSignatureForSelector:sel]);
}
@end

///---------------------------------------------------------------------------------------
#pragma mark - BLEDeviceManager Class Implementation
///---------------------------------------------------------------------------------------

@implementation BLEDeviceManager

- (instancetype)init {
    self = [super init];
    if (self) {
#ifdef BLE_DEVICE_OPTION_CALLBACK_USING_MAIN_QUEUE
        NSLog(@"defined BLE_DEVICE_OPTION_CALLBACK_USING_MAIN_QUEUE");
#endif // BLE_DEVICE_OPTION_CALLBACK_USING_MAIN_QUEUE
#ifdef BLE_DEVICE_OPTION_ENABLE_RETRY_FOR_NOTIFICATION_ACTIVATION
        NSLog(@"defined BLE_DEVICE_OPTION_ENABLE_RETRY_FOR_NOTIFICATION_ACTIVATION");
        NSLog(@"defined BLE_DEVICE_OPTION_RETRY_INTERVAL_FOR_NOTIFICATION_ACTIVATION %f", BLE_DEVICE_OPTION_RETRY_INTERVAL_FOR_NOTIFICATION_ACTIVATION);
        NSLog(@"defined BLE_DEVICE_OPTION_RETRY_COUNT_FOR_NOTIFICATION_ACTIVATION %d", BLE_DEVICE_OPTION_RETRY_COUNT_FOR_NOTIFICATION_ACTIVATION);
#endif // BLE_DEVICE_OPTION_ENABLE_RETRY_FOR_NOTIFICATION_ACTIVATION
        
        CBCentralManager *central = [[CBCentralManager alloc] initWithDelegate:self queue:ble_device_dispatch_get_internal_queue() options:@{CBCentralManagerOptionShowPowerAlertKey: @NO}];
        self.central = (CBCentralManager *)[[LogProxy alloc] initWithObject:central];
        
        while (self.central.state == CBCentralManagerStateUnknown || self.central.state == CBCentralManagerStateResetting) {
            [NSThread sleepForTimeInterval:0.1];
        }
        [self ble_convertCBManagerState:self.central.state toBLEDeviceManagerState:&_state];
        
        _scanCacheDictionary = [@{} mutableCopy];
        _scanStartBlock = NULL;
        _scanObserverBlock = NULL;
        _scanCompletionBlock = NULL;
        
        _deviceDataReadingCallbackBundles = [@{} mutableCopy];
        
        _bloodPressureServiceUUID = [CBUUID UUIDWithString:BloodPressureServiceUUIDString];
        _batteryServiceUUID = [CBUUID UUIDWithString:BatteryServiceUUIDString];
        _currentTimeServiceUUID = [CBUUID UUIDWithString:CurrentTimeServiceUUIDString];
        _weightScaleServiceUUID = [CBUUID UUIDWithString:WeightScaleServiceUUIDString];
        _bloodPressureMeasurementCharacteristicUUID = [CBUUID UUIDWithString:BloodPressureMeasurementCharacteristicUUIDString];
        _bloodPressureFeatureCharacteristicUUID = [CBUUID UUIDWithString:BloodPressureFeatureCharacteristicUUIDString];
        _batteryLevelCharacteristicUUID = [CBUUID UUIDWithString:BatteryLevelCharacteristicUUIDString];
        _currentTimeCharacteristicUUID = [CBUUID UUIDWithString:CurrentTimeCharacteristicUUIDString];
        _weightMeasurementCharacteristicUUID = [CBUUID UUIDWithString:WeightMeasurementCharacteristicUUIDString];
        _weightFeatureCharacteristicUUID = [CBUUID UUIDWithString:WeightFeatureCharacteristicUUIDString];
        
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(ble_observeNotification:) name:UIApplicationDidEnterBackgroundNotification object:nil];
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(ble_observeNotification:) name:UIApplicationWillEnterForegroundNotification object:nil];
    }
    return self;
}

- (void)dealloc {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

///---------------------------------------------------------------------------------------
#pragma mark - Public Methods
///---------------------------------------------------------------------------------------

+ (BLEDeviceManager *)sharedManager {
    static BLEDeviceManager *_sharedManager = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        _sharedManager = [[BLEDeviceManager alloc] init];
    });
    return _sharedManager;
}

+ (NSString *)deviceManagerStateName:(BLEDeviceManagerState)state {
    switch (state) {
        case BLEDeviceManagerStateUnknown:
            return @"BLEDeviceManagerStateUnknown";
        case BLEDeviceManagerStateUnsupported:
            return @"BLEDeviceManagerStateUnsupported";
        case BLEDeviceManagerStateUnauthorized:
            return @"BLEDeviceManagerStateUnauthorized";
        case BLEDeviceManagerStatePoweredOff:
            return @"BLEDeviceManagerStatePoweredOff";
        case BLEDeviceManagerStatePoweredOn:
            return @"BLEDeviceManagerStatePoweredOn";
            
        default:
            return nil;
    }
}

+ (NSString *)connectionStateName:(BLEDeviceConnectionState)state {
    switch (state) {
        case BLEDeviceConnectionStateDisconnected:
            return @"BLEDeviceConnectionStateDisconnected";
        case BLEDeviceConnectionStateConnecting:
            return @"BLEDeviceConnectionStateConnecting";
        case BLEDeviceConnectionStateConnected:
            return @"BLEDeviceConnectionStateConnected";
        case BLEDeviceConnectionStateDisconnecting:
            return @"BLEDeviceConnectionStateDisconnecting";
            
        default:
            return nil;
    }
}

+ (NSString *)completionReasonName:(BLEDeviceCompletionReason)reason {
    switch (reason) {
        case BLEDeviceCompletionReasonCanceled:
            return @"BLEDeviceCompletionReasonCanceled";
        case BLEDeviceCompletionReasonPoweredOff:
            return @"BLEDeviceCompletionReasonPoweredOff";
        case BLEDeviceCompletionReasonInvalidDeviceIdentifier:
            return @"BLEDeviceCompletionReasonInvalidDeviceIdentifier";
        case BLEDeviceCompletionReasonDisconnected:
            return @"BLEDeviceCompletionReasonDisconnected";
        case BLEDeviceCompletionReasonFailedToConnect:
            return @"BLEDeviceCompletionReasonFailedToConnect";
        case BLEDeviceCompletionReasonFailedToTransfer:
            return @"BLEDeviceCompletionReasonFailedToTransfer";
        case BLEDeviceCompletionReasonBusy:
            return @"BLEDeviceCompletionReasonBusy";
            
        default:
            return nil;
    }
}

+ (NSString *)characteristicTypeName:(BLEDeviceCharacteristicType)type {
    switch (type) {
        case BLEDeviceCharacteristicTypeCurrentTime:
            return @"BLEDeviceCharacteristicTypeCurrentTime";
        case BLEDeviceCharacteristicTypeBatteryLevel:
            return @"BLEDeviceCharacteristicTypeBatteryLevel";
        case BLEDeviceCharacteristicTypeBloodPressureData:
            return @"BLEDeviceCharacteristicTypeBloodPressureData";
        case BLEDeviceCharacteristicTypeWeightData:
            return @"BLEDeviceCharacteristicTypeWeightData";
            
        default:
            return nil;
    }
}



- (void)scanForDevicesWithCategories:(BLEDeviceCategory)categories observer:(BLEDeviceScanObserverBlock)observer completion:(BLEDeviceCompletionBlock)completion {
    NSLog(@"");
    
    // Check Parameter.
    if (!completion) {
        NSLog(@"nil == completion");
        @throw [NSException exceptionWithName:NSInvalidArgumentException
                                       reason:[NSString stringWithFormat:@"%s: completion cannot be nil", __PRETTY_FUNCTION__]
                                     userInfo:nil];
        return;
    }
    
   
    
    NSMutableArray<CBUUID *> *services = [@[] mutableCopy];
    /* 原过滤方法*/
    if (categories & BLEDeviceCategoryBloodPressure) {
        [services addObject:self.bloodPressureServiceUUID];
    }
    if (categories & BLEDeviceCategoryWeightScale) {
        [services addObject:self.weightScaleServiceUUID];
    }
    if (categories & BLEDeviceCategoryBloodSugar) {
        [services addObject:self.batteryServiceUUID];
    }
    if (!services.count) {
        services = nil;
    }
    
    __weak typeof(self) weakSelf = self;
    ble_device_dispatch_to_internal_queue(^{
        
        // Check the Bluetooth state.
        if (weakSelf.state != BLEDeviceManagerStatePoweredOn) {
            ble_device_dispatch_to_callback_queue(^{
                if (completion) {
                    completion(BLEDeviceCompletionReasonPoweredOff);
                }
            });
            return;
        }
        
        // Check the scanning.
        if (weakSelf.scanCompletionBlock) {
            ble_device_dispatch_to_callback_queue(^{
                if (completion) {
                    completion(BLEDeviceCompletionReasonBusy);
                }
            });
            return;
        }
        
        // Clear the scan cache.
        [weakSelf.scanCacheDictionary removeAllObjects];
//        @"1810" @[[CBUUID UUIDWithString:@"1810"]]  services
        weakSelf.scanStartBlock = ^{
            [weakSelf.central scanForPeripheralsWithServices:@[[CBUUID UUIDWithString:@"1810"]] options:@{CBCentralManagerScanOptionAllowDuplicatesKey: @YES}];
        };
        weakSelf.scanObserverBlock = ^(NSDictionary<NSString *, id> *deviceInfo) {
            if (observer) {
                ble_device_dispatch_to_callback_queue(^{
                    observer(deviceInfo);
                });
            }
        };
        weakSelf.scanCompletionBlock = ^(BLEDeviceCompletionReason aReason) {
            NSLog(@"Stop BLE Scan");
            [weakSelf.central stopScan];
            weakSelf.scanStartBlock = NULL;
            weakSelf.scanObserverBlock = NULL;
            weakSelf.scanCompletionBlock = NULL;
            if (completion) {
                ble_device_dispatch_to_callback_queue(^{
                    completion(aReason);
                });
            }
        };
        //add start
        
        //add end
        
        // Start to scan.
        NSLog(@"Start BLE Scan");
        weakSelf.scanStartBlock();
    });
}

- (void)stopScan {
    NSLog(@"");
    
    __weak typeof(self) weakSelf = self;
    ble_device_dispatch_to_internal_queue(^{
        if (weakSelf.scanCompletionBlock) {
            weakSelf.scanCompletionBlock(BLEDeviceCompletionReasonCanceled);
        }
    });
}

- (void)readDataFromDeviceWithIdentifier:(NSUUID *)identifier dataObserver:(BLEDeviceDataObserverBlock)dataObserver connectionObserver:(BLEDeviceConnectionObserverBlock)connectionObserver completion:(BLEDeviceCompletionBlock)completion {
    NSLog(@"");

    // Check Parameter.
    if (!identifier) {
        NSLog(@"nil == identifier");
        @throw [NSException exceptionWithName:NSInvalidArgumentException
                                       reason:[NSString stringWithFormat:@"%s: identifier cannot be nil", __PRETTY_FUNCTION__]
                                     userInfo:nil];
        return;
    }
    if (!completion) {
        NSLog(@"nil == completion");
        @throw [NSException exceptionWithName:NSInvalidArgumentException
                                       reason:[NSString stringWithFormat:@"%s: completion cannot be nil", __PRETTY_FUNCTION__]
                                     userInfo:nil];
        return;
    }
    
    __weak typeof(self) weakSelf = self;
    ble_device_dispatch_to_internal_queue(^{
        
        // Check the Bluetooth state.
        if (weakSelf.state != BLEDeviceManagerStatePoweredOn) {
            if (completion) {
                ble_device_dispatch_to_callback_queue(^{
                    completion(BLEDeviceCompletionReasonPoweredOff);
                });
            }
            return;
        }
        
        // Get Peripheral object to read data.
        CBPeripheral *peripheral = weakSelf.scanCacheDictionary[identifier].peripheral;
        if (!peripheral) {
            NSArray<CBPeripheral *> *peripherals = [weakSelf.central retrievePeripheralsWithIdentifiers:@[identifier]];
            peripheral = peripherals.firstObject;
            if (!peripherals) {
                if (completion) {
                    ble_device_dispatch_to_callback_queue(^{
                        completion(BLEDeviceCompletionReasonInvalidDeviceIdentifier);
                    });
                }
                return;
            }
        }
        
        // Checks if the data reading.
        if (weakSelf.deviceDataReadingCallbackBundles[peripheral]) {
            if (completion) {
                ble_device_dispatch_to_callback_queue(^{
                    completion(BLEDeviceCompletionReasonBusy);
                });
            }
            return;
        }
        
        BLEDeviceDataReadingCallbackBundle *callbackBundle = [[BLEDeviceDataReadingCallbackBundle alloc] initWithDataObserver:^(BLEDeviceCharacteristicType aCharacteristicType, NSDictionary<NSString *, id> * _Nonnull data) {
            if (dataObserver) {
                ble_device_dispatch_to_callback_queue(^{
                    dataObserver(aCharacteristicType, data);
                });
            }
        } connectionObserver:^(BLEDeviceConnectionState aState) {
            if (connectionObserver) {
                ble_device_dispatch_to_callback_queue(^{
                    connectionObserver(aState);
                });
            }
        } completion:^(BLEDeviceCompletionReason aReason) {
            ble_device_dispatch_to_callback_queue(^{
                weakSelf.deviceDataReadingCallbackBundles[peripheral] = nil;
                if (completion) {
                    completion(aReason);
                };
            });
        }];
        weakSelf.deviceDataReadingCallbackBundles[peripheral] = callbackBundle;
        
        peripheral.delegate = self;
        [weakSelf.central connectPeripheral:peripheral options:nil];
        weakSelf.deviceDataReadingCallbackBundles[peripheral].connectionObserverBlock(BLEDeviceConnectionStateConnecting);
    });
}

- (void)cancelReadingFromDeviceWithIdentifier:(NSUUID *)identifier {
    NSLog(@"");
    
    __block CBPeripheral *peripheral = nil;
    __block BLEDeviceDataReadingCallbackBundle *callbackBundle = nil;
    
    [self.deviceDataReadingCallbackBundles enumerateKeysAndObjectsUsingBlock:^(CBPeripheral * _Nonnull key, BLEDeviceDataReadingCallbackBundle * _Nonnull obj, BOOL * _Nonnull stop) {
        if ([key.identifier isEqual:identifier]) {
            peripheral = key;
            callbackBundle = obj;
            *stop = YES;
        }
    }];
    
    if (!peripheral) {
        NSLog(@"nil == peripheral");
        return;
    }
    
    BLEDeviceCompletionBlock compleationBlock = callbackBundle.completionBlock;
    callbackBundle.completionBlock = ^(BLEDeviceCompletionReason reason) {
        compleationBlock(BLEDeviceCompletionReasonCanceled);
    };
    
    callbackBundle.connectionObserverBlock(BLEDeviceConnectionStateDisconnecting);
    [self.central cancelPeripheralConnection:peripheral];
}

///---------------------------------------------------------------------------------------
#pragma mark - Private methods
///---------------------------------------------------------------------------------------

- (BOOL)ble_convertCBManagerState:(CBManagerState)inState toBLEDeviceManagerState:(BLEDeviceManagerState *)outState {
    NSLog(@"");
    
    BOOL ret = NO;
    if (outState) {
        switch (inState) {
            case CBManagerStateUnknown:
            case CBManagerStateResetting:
                *outState = BLEDeviceManagerStateUnknown;
                ret = YES;
                break;
            case CBManagerStateUnsupported:
                *outState = BLEDeviceManagerStateUnsupported;
                ret = YES;
                break;
            case CBManagerStateUnauthorized:
                *outState = BLEDeviceManagerStateUnauthorized;
                ret = YES;
                break;
            case CBManagerStatePoweredOff:
                *outState = BLEDeviceManagerStatePoweredOff;
                ret = YES;
                break;
            case CBManagerStatePoweredOn:
                *outState = BLEDeviceManagerStatePoweredOn;
                ret = YES;
                break;
            default:
                break;
        }
    }
    return ret;
}

// !!!: The stop and restart scannning processes for BLE devices are implemented at the time of moving to backgournd only for iOS10 because these processes are implemented by Framework automatically in version prior to iOS10.
- (void)ble_observeNotification:(NSNotification *)notification {
    NSLog(@"");
    
    if ([notification.name isEqualToString:UIApplicationDidEnterBackgroundNotification]) {
        NSLog(@"Stop BLE Scan");
        [self.central stopScan];
    }
    else if ([notification.name isEqualToString:UIApplicationWillEnterForegroundNotification]) {
        __weak typeof(self) weakSelf = self;
        ble_device_dispatch_to_internal_queue(^{
            NSLog(@"Start BLE Scan");
//            [self sendEventWithName:@"BleManagerDidUpdateState" body:@{@"state":@""}];

//            [[BleManager manager] sendEventWithName:@"BleManagerDidUpdateState" body:@{@"state":@""}];
        
            if (weakSelf.scanStartBlock) {
                weakSelf.scanStartBlock();
//                 [[BleManager getCentralManager] _scanForDevices];
//                [[BleManager getInstance]  _scanForDevices];
            } else {
//                 [[BleManager getInstance]  _scanForDevices];
            }
        });
    }
}

- (void)ble_abortDataReadingForPeripheral:(CBPeripheral *)peripheral byReason:(BLEDeviceCompletionReason)reason {
    BLEDeviceDataReadingCallbackBundle *callbackBundle = self.deviceDataReadingCallbackBundles[peripheral];
    NSLog(@"");
    
    if (!callbackBundle) {
        NSLog(@"nil == callbackBundle");
        return;
    }
    
    if (peripheral.state != CBPeripheralStateConnected) {
        NSLog(@"peripheral.state != CBPeripheralStateConnected");
        return;
    }
    
    BLEDeviceCompletionBlock compleationBlock = callbackBundle.completionBlock;
    callbackBundle.completionBlock = ^(BLEDeviceCompletionReason aReason) {
        compleationBlock(reason);
    };
    callbackBundle.connectionObserverBlock(BLEDeviceConnectionStateDisconnecting);
    [self.central cancelPeripheralConnection:peripheral];
}

///---------------------------------------------------------------------------------------
#pragma mark - CBCentralManager delegate
///---------------------------------------------------------------------------------------

- (void)centralManagerDidUpdateState:(CBCentralManager *)central {
    NSLog(@"");
    
    BLEDeviceManagerState state;
    [self ble_convertCBManagerState:self.central.state toBLEDeviceManagerState:&state];
    self.state = state;
    NSLog(@"%@", [BLEDeviceManager deviceManagerStateName:state]);
    if (self.state != BLEDeviceManagerStatePoweredOn) {
        if (self.scanCompletionBlock) {
            self.scanCompletionBlock(BLEDeviceCompletionReasonPoweredOff);
        }
        if (self.deviceDataReadingCallbackBundles.count) {
            [self.deviceDataReadingCallbackBundles.allValues enumerateObjectsUsingBlock:^(BLEDeviceDataReadingCallbackBundle * _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
                obj.completionBlock(BLEDeviceCompletionReasonPoweredOff);
            }];
        }
    }
}

- (void)centralManager:(CBCentralManager *)central didDiscoverPeripheral:(CBPeripheral *)peripheral advertisementData:(NSDictionary<NSString *, id> *)advertisementData RSSI:(NSNumber *)RSSI {
    NSLog(@"=======miao======%@", peripheral.identifier.UUIDString);

    NSUUID *identifier = peripheral.identifier;
    NSDictionary<NSString *, id> *deviceInfo = nil;
    
    BLEDevice *cachedDevice = self.scanCacheDictionary[identifier];
    if (!cachedDevice) {
        // new device
        BLEDevice *newDevice = [[BLEDevice alloc] initWithPeripheral:peripheral advertisementData:advertisementData RSSI:RSSI];
        self.scanCacheDictionary[peripheral.identifier] = newDevice;
        deviceInfo = newDevice.deviceInfo;
    }
    else {
        // update device
        [cachedDevice updateWithAdvertisementData:advertisementData RSSI:RSSI];
        deviceInfo = cachedDevice.deviceInfo;
    }
    
    if (deviceInfo) {
//         [[BleManager ma] sendEventWithName:@"BleManagerDiscoverPeripheral" body:[peripheral asDictionary]];

        self.scanObserverBlock(deviceInfo);
    }
}



- (void)centralManager:(CBCentralManager *)central didConnectPeripheral:(CBPeripheral *)peripheral {
    NSLog(@"%@", peripheral.identifier.UUIDString);
    
    BLEDeviceDataReadingCallbackBundle *callbackBundle = self.deviceDataReadingCallbackBundles[peripheral];
    if (!callbackBundle) {
        NSLog(@"nil == callbackBundle");
        return;
    }
    NSLog(@"Connect to %@(%@)", peripheral.name, peripheral.identifier.UUIDString);
    callbackBundle.connectionObserverBlock(BLEDeviceConnectionStateConnected);
    NSArray<CBUUID *> *serviceUUIDs = @[self.bloodPressureServiceUUID, self.batteryServiceUUID, self.currentTimeServiceUUID, self.weightScaleServiceUUID];
    [peripheral discoverServices:serviceUUIDs];
}

- (void)centralManager:(CBCentralManager *)central didFailToConnectPeripheral:(CBPeripheral *)peripheral error:(nullable NSError *)error {
    NSLog(@"%@", peripheral.identifier.UUIDString);
    if (error) {
        NSLog(@"%@", [error localizedDescription]);
    }
    
    BLEDeviceDataReadingCallbackBundle *callbackBundle = self.deviceDataReadingCallbackBundles[peripheral];
    if (!callbackBundle) {
        NSLog(@"nil == callbackBundle");
        return;
    }
    callbackBundle.connectionObserverBlock(BLEDeviceConnectionStateDisconnected);
    callbackBundle.completionBlock(BLEDeviceCompletionReasonFailedToConnect);
}

- (void)centralManager:(CBCentralManager *)central didDisconnectPeripheral:(CBPeripheral *)peripheral error:(nullable NSError *)error {
    NSLog(@"%@", peripheral.identifier.UUIDString);
    if (error) {
        NSLog(@"%@", [error localizedDescription]);
    }
    NSLog(@"Disconnection by peripheral or OS.");
    
    BLEDeviceDataReadingCallbackBundle *callbackBundle = self.deviceDataReadingCallbackBundles[peripheral];
    if (!callbackBundle) {
        NSLog(@"nil == callbackBundle");
        return;
    }
    callbackBundle.connectionObserverBlock(BLEDeviceConnectionStateDisconnected);
    callbackBundle.completionBlock(BLEDeviceCompletionReasonDisconnected);
}

///---------------------------------------------------------------------------------------
#pragma mark - CBPeripheral delegate
///---------------------------------------------------------------------------------------

- (void)peripheral:(CBPeripheral *)peripheral didDiscoverServices:(NSError *)error {
    NSLog(@"%@", peripheral.identifier.UUIDString);
    
    BLEDeviceDataReadingCallbackBundle *callbackBundle = self.deviceDataReadingCallbackBundles[peripheral];
    if (!callbackBundle) {
        NSLog(@"nil == callbackBundle");
        return;
    }
    
    if (error) {
        [self ble_abortDataReadingForPeripheral:peripheral byReason:BLEDeviceCompletionReasonFailedToTransfer];
        NSLog(@"%@", [error localizedDescription]);
        return;
    }
    
    [peripheral.services enumerateObjectsUsingBlock:^(CBService * _Nonnull service, NSUInteger idx, BOOL * _Nonnull stop) {
        if ([service.UUID isEqual:self.bloodPressureServiceUUID]) {
            NSArray<CBUUID *> *characteristicUUIDs = @[self.bloodPressureMeasurementCharacteristicUUID, self.bloodPressureFeatureCharacteristicUUID];
            [peripheral discoverCharacteristics:characteristicUUIDs forService:service];
        }
        else if ([service.UUID isEqual:self.batteryServiceUUID]) {
            NSArray<CBUUID *> *characteristicUUIDs = @[self.batteryLevelCharacteristicUUID];
            [peripheral discoverCharacteristics:characteristicUUIDs forService:service];
        }
        else if ([service.UUID isEqual:self.currentTimeServiceUUID]) {
            NSArray<CBUUID *> *characteristicUUIDs = @[self.currentTimeCharacteristicUUID];
            [peripheral discoverCharacteristics:characteristicUUIDs forService:service];
        }
        else if ([service.UUID isEqual:self.weightScaleServiceUUID]) {
            NSArray<CBUUID *> *characteristicUUIDs = @[self.weightMeasurementCharacteristicUUID, self.weightFeatureCharacteristicUUID];
            [peripheral discoverCharacteristics:characteristicUUIDs forService:service];
        }
    }];
}

- (void)peripheral:(CBPeripheral *)peripheral didDiscoverCharacteristicsForService:(CBService *)service error:(NSError *)error {
    NSLog(@"%@", peripheral.identifier.UUIDString);
    
    BLEDeviceDataReadingCallbackBundle *callbackBundle = self.deviceDataReadingCallbackBundles[peripheral];
    if (!callbackBundle) {
        NSLog(@"nil == callbackBundle");
        return;
    }
    
    if (error) {
        [self ble_abortDataReadingForPeripheral:peripheral byReason:BLEDeviceCompletionReasonFailedToTransfer];
        NSLog(@"%@", [error localizedDescription]);
        return;
    }
    
    if ([service.UUID isEqual:self.bloodPressureServiceUUID]) {
        [service.characteristics enumerateObjectsUsingBlock:^(CBCharacteristic * _Nonnull characteristic, NSUInteger idx, BOOL * _Nonnull stop) {
            if ([characteristic.UUID isEqual:self.bloodPressureMeasurementCharacteristicUUID]) {
                [peripheral setNotifyValue:YES forCharacteristic:characteristic];
            } else if ([characteristic.UUID isEqual:self.bloodPressureFeatureCharacteristicUUID]) {
                [peripheral readValueForCharacteristic:characteristic];
            }
        }];
    }
    else if ([service.UUID isEqual:self.batteryServiceUUID]) {
        [service.characteristics enumerateObjectsUsingBlock:^(CBCharacteristic * _Nonnull characteristic, NSUInteger idx, BOOL * _Nonnull stop) {
            if ([characteristic.UUID isEqual:self.batteryLevelCharacteristicUUID]) {
                [peripheral setNotifyValue:YES forCharacteristic:characteristic];
            }
        }];
    }
    else if ([service.UUID isEqual:self.currentTimeServiceUUID]) {
        [service.characteristics enumerateObjectsUsingBlock:^(CBCharacteristic * _Nonnull characteristic, NSUInteger idx, BOOL * _Nonnull stop) {
            if ([characteristic.UUID isEqual:self.currentTimeCharacteristicUUID]) {
                [peripheral setNotifyValue:YES forCharacteristic:characteristic];
            }
        }];
    }
    else if ([service.UUID isEqual:self.weightScaleServiceUUID]) {
        [service.characteristics enumerateObjectsUsingBlock:^(CBCharacteristic * _Nonnull characteristic, NSUInteger idx, BOOL * _Nonnull stop) {
            if ([characteristic.UUID isEqual:self.weightMeasurementCharacteristicUUID]) {
                [peripheral setNotifyValue:YES forCharacteristic:characteristic];
            } else if ([characteristic.UUID isEqual:self.weightFeatureCharacteristicUUID]) {
                [peripheral readValueForCharacteristic:characteristic];
            }
        }];
    }
}

- (void)peripheral:(CBPeripheral *)peripheral didUpdateNotificationStateForCharacteristic:(nonnull CBCharacteristic *)characteristic error:(nullable NSError *)error {
    NSLog(@"%@", peripheral.identifier.UUIDString);
    
    BLEDeviceDataReadingCallbackBundle *callbackBundle = self.deviceDataReadingCallbackBundles[peripheral];
    if (!callbackBundle) {
        NSLog(@"nil == callbackBundle");
        return;
    }
    
    if (error) {
        NSLog(@"%@", [error localizedDescription]);
#ifdef BLE_DEVICE_OPTION_ENABLE_RETRY_FOR_NOTIFICATION_ACTIVATION
        if ([error.domain isEqualToString:CBATTErrorDomain] && error.code == CBATTErrorInsufficientAuthentication && callbackBundle.retryCount < BLE_DEVICE_OPTION_RETRY_COUNT_FOR_NOTIFICATION_ACTIVATION) {
            callbackBundle.retryCount += 1;
            dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(BLE_DEVICE_OPTION_RETRY_INTERVAL_FOR_NOTIFICATION_ACTIVATION * NSEC_PER_SEC)), ble_device_dispatch_get_internal_queue(), ^{
                [peripheral setNotifyValue:YES forCharacteristic:characteristic];
            });
        }
        else {
            [self ble_abortDataReadingForPeripheral:peripheral byReason:BLEDeviceCompletionReasonFailedToTransfer];
        }
#else // BLE_DEVICE_OPTION_ENABLE_RETRY_FOR_NOTIFICATION_ACTIVATION
        [self ble_abortDataReadingForPeripheral:peripheral byReason:BLEDeviceCompletionReasonFailedToTransfer];
#endif // BLE_DEVICE_OPTION_ENABLE_RETRY_FOR_NOTIFICATION_ACTIVATION
        return;
    }
}

- (void)peripheral:(CBPeripheral *)peripheral didUpdateValueForCharacteristic:(CBCharacteristic *)characteristic error:(NSError *)error {
    NSLog(@"%@", peripheral.identifier.UUIDString);
    
    BLEDeviceDataReadingCallbackBundle *callbackBundle = self.deviceDataReadingCallbackBundles[peripheral];
    if (!callbackBundle) {
        NSLog(@"nil == callbackBundle");
        return;
    }
    
    if (error) {
        [self ble_abortDataReadingForPeripheral:peripheral byReason:BLEDeviceCompletionReasonFailedToTransfer];
        NSLog(@"%@", [error localizedDescription]);
        return;
    }
    
    if ([characteristic.UUID isEqual:self.currentTimeCharacteristicUUID]) {
        NSLog(@"Current Time Raw Data: %@", characteristic.value);
        CurrentTime currentTimeValue;
        memcpy(&currentTimeValue, characteristic.value.bytes, sizeof(currentTimeValue));
        
        callbackBundle.dataObserverBlock(BLEDeviceCharacteristicTypeCurrentTime, @{BLEDeviceCurrentTimeKey: ble_device_convert_to_nsdate(currentTimeValue.dateTime)});
    }
    else if ([characteristic.UUID isEqual:self.batteryLevelCharacteristicUUID]) {
        NSLog(@"Battery Level Raw Data: %@", characteristic.value);
        UInt8 batteryLevelValue;
        memcpy(&batteryLevelValue, characteristic.value.bytes, sizeof(batteryLevelValue));
        
        callbackBundle.dataObserverBlock(BLEDeviceCharacteristicTypeBatteryLevel, @{BLEDeviceBatteryLevelKey: @(batteryLevelValue)});
    }
    else if ([characteristic.UUID isEqual:self.bloodPressureMeasurementCharacteristicUUID]) {
        NSLog(@"Blood Pressure Measurement Raw Data: %@", characteristic.value);
        NSMutableDictionary *dict = [@{} mutableCopy];
        const void *pt = characteristic.value.bytes;
        
        BloodPressureMeasurementFlag flagsValue;
        memcpy(&flagsValue, pt, sizeof(flagsValue));
        dict[BLEDeviceBloodPressureDataFlagsKey] = @(flagsValue);
        dict[BLEDeviceBloodPressureDataUnitKey] = ((flagsValue & BloodPressureMeasurementFlagKpaUnit) ? @"kPa" : @"mmHg");
        pt += sizeof(flagsValue);
        
        SFloat systolicValue;
        memcpy(&systolicValue, pt, sizeof(systolicValue));
        dict[BLEDeviceBloodPressureDataSystolicKey] = @(ble_device_convert_to_float32(systolicValue));
        pt += sizeof(systolicValue);
        
        SFloat diastolicValue;
        memcpy(&diastolicValue, pt, sizeof(diastolicValue));
        dict[BLEDeviceBloodPressureDataDiastolicKey] = @(ble_device_convert_to_float32(diastolicValue));
        pt += sizeof(diastolicValue);
        
        SFloat meanArterialPressureValue;
        memcpy(&meanArterialPressureValue, pt, sizeof(meanArterialPressureValue));
        dict[BLEDeviceBloodPressureDataMeanArterialPressureKey] = @(ble_device_convert_to_float32(meanArterialPressureValue));
        pt += sizeof(meanArterialPressureValue);
        
        if (flagsValue & BloodPressureMeasurementFlagTimeStampPresent) {
            DateTime timeStampValue;
            memcpy(&timeStampValue, pt, sizeof(timeStampValue));
            dict[BLEDeviceBloodPressureDataTimeStampKey] = ble_device_convert_to_nsdate(timeStampValue);
            pt += sizeof(timeStampValue);
        }
        
        if (flagsValue & BloodPressureMeasurementFlagPulseRatePresent) {
            SFloat pulseRateValue;
            memcpy(&pulseRateValue, pt, sizeof(pulseRateValue));
            dict[BLEDeviceBloodPressureDataPulseRateKey] = @(ble_device_convert_to_float32(pulseRateValue));
            pt += sizeof(pulseRateValue);
        }
        
        if (flagsValue & BloodPressureMeasurementFlagUserIDPresent) {
            UInt8 userIDValue;
            memcpy(&userIDValue, pt, sizeof(userIDValue));
            dict[BLEDeviceBloodPressureDataUserIDKey] = @(userIDValue);
            pt += sizeof(userIDValue);
        }
        
        if (flagsValue & BloodPressureMeasurementFlagStatusPresent) {
            UInt16 measurementStatusValue;
            memcpy(&measurementStatusValue, pt, sizeof(measurementStatusValue));
            dict[BLEDeviceBloodPressureDataMeasurementStatusKey] = @(measurementStatusValue);
        }
        
        callbackBundle.dataObserverBlock(BLEDeviceCharacteristicTypeBloodPressureData, dict);
    }
    else if ([characteristic.UUID isEqual:self.bloodPressureFeatureCharacteristicUUID]) {
        NSLog(@"Blood Pressure Feature Raw Data: %@", characteristic.value);
        callbackBundle.dataObserverBlock(BLEDeviceCharacteristicTypeBloodPressureFeature, @{BLEDeviceBloodPressureFeatureKey: characteristic.value});
    }
    else if ([characteristic.UUID isEqual:self.weightMeasurementCharacteristicUUID]) {
        NSLog(@"Weight Measurement Raw Data: %@", characteristic.value);
        NSMutableDictionary *dict = [@{} mutableCopy];
        const void *pt = characteristic.value.bytes;
        
        WeightMeasurementFlags flagsValue;
        memcpy(&flagsValue, pt, sizeof(flagsValue));
        dict[BLEDeviceWeightDataFlagsKey] = @(flagsValue);
        dict[BLEDeviceWeightDataWeightUnitKey] = ((flagsValue & WeightMeasurementFlagImperialUnit) ? @"lb" : @"kg");
        pt += sizeof(flagsValue);
        
        UInt16 weightValue;
        memcpy(&weightValue, pt, sizeof(weightValue));
        dict[BLEDeviceWeightDataWeightKey] = @(weightValue);
        pt += sizeof(weightValue);
        
        if (flagsValue & WeightMeasurementFlagTimeStampPresent) {
            DateTime timeStampValue;
            memcpy(&timeStampValue, pt, sizeof(timeStampValue));
            dict[BLEDeviceWeightDataTimeStampKey] = ble_device_convert_to_nsdate(timeStampValue);
            pt += sizeof(timeStampValue);
        }
        
        if (flagsValue & WeightMeasurementFlagUserIDPresent) {
            UInt8 userIDValue;
            memcpy(&userIDValue, pt, sizeof(userIDValue));
            dict[BLEDeviceWeightDataUserIDKey] = @(userIDValue);
            pt += sizeof(userIDValue);
        }
        
        if (flagsValue & WeightMeasurementFlagBMIAndHeightPresent) {
            dict[BLEDeviceWeightDataHeightUnitKey] = ((flagsValue & WeightMeasurementFlagImperialUnit) ? @"in" : @"m");
            
            UInt16 BMIValue;
            memcpy(&BMIValue, pt, sizeof(BMIValue));
            dict[BLEDeviceWeightDataBMIKey] = @(BMIValue);
            pt += sizeof(BMIValue);
            
            UInt16 heightValue;
            memcpy(&heightValue, pt, sizeof(heightValue));
            dict[BLEDeviceWeightDataHeightKey] = @(heightValue);
        }
        
        callbackBundle.dataObserverBlock(BLEDeviceCharacteristicTypeWeightData, dict.copy);
    }
    else if ([characteristic.UUID isEqual:self.weightFeatureCharacteristicUUID]) {
        NSLog(@"Weight Feature Raw Data: %@", characteristic.value);
        callbackBundle.dataObserverBlock(BLEDeviceCharacteristicTypeWeightFeature, @{BLEDeviceWeightFeatureKey: characteristic.value});
    }
}

@end
