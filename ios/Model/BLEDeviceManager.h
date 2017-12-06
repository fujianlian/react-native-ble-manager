//
//  BLEDeviceManager.h
//  BleSampleOmron
//
//  Copyright © 2016 Omron Healthcare Co., Ltd. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BLEDeviceDefines.h"

NS_ASSUME_NONNULL_BEGIN

///---------------------------------------------------------------------------------------
#pragma mark - BLEDeviceManager interface
///---------------------------------------------------------------------------------------

@interface BLEDeviceManager : NSObject

+ (BLEDeviceManager *)sharedManager;

+ (NSString *)deviceManagerStateName:(BLEDeviceManagerState)state;
+ (NSString *)connectionStateName:(BLEDeviceConnectionState)state;
+ (NSString *)completionReasonName:(BLEDeviceCompletionReason)reason;
+ (NSString *)characteristicTypeName:(BLEDeviceCharacteristicType)type;

/** Scan the device.
 @param categories Device classification to scan
 @param observer Scan monitoring block
 @param completion Complete process block
 */
- (void)scanForDevicesWithCategories:(BLEDeviceCategory)categories
                            observer:(BLEDeviceScanObserverBlock)observer
                          completion:(BLEDeviceCompletionBlock)completion;

/** Suspend the Scanning.
 */
- (void)stopScan;

/** Reads from the device with the specified identifer.
 @param identifier Identifier of device
 @param dataObserver Data monitoring block
 @param connectionObserver Connection monitoring block
 @param completion Complete process block
 */
- (void)readDataFromDeviceWithIdentifier:(NSUUID *)identifier
                            dataObserver:(BLEDeviceDataObserverBlock)dataObserver
                      connectionObserver:(BLEDeviceConnectionObserverBlock)connectionObserver
                              completion:(BLEDeviceCompletionBlock)completion;

/** Cancels reading from the device with the specified identifier.
 @param identifier Identifier of device
 */
- (void)cancelReadingFromDeviceWithIdentifier:(NSUUID *)identifier;

@property (nonatomic, assign, readonly) BLEDeviceManagerState state;

@end

NS_ASSUME_NONNULL_END
