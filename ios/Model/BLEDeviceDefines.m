//
//  BLEDeviceDefines.m
//  BleSampleOmron
//
//  Copyright © 2016 Omron Healthcare Co., Ltd. All rights reserved.
//

#import "BLEDeviceDefines.h"

// Device Information key.
NSString * const BLEDeviceInfoAdvertisementDataKey = @"advertisementData";
NSString * const BLEDeviceInfoIdentifierKey = @"identifier";
NSString * const BLEDeviceInfoLocalNameKey = @"localName";
NSString * const BLEDeviceInfoModelNameKey = @"modelName";
NSString * const BLEDeviceInfoRSSIKey = @"RSSI";

// CurrentTime key.
NSString * const BLEDeviceCurrentTimeKey = @"currentTime";

// BatteryLevel key.
NSString * const BLEDeviceBatteryLevelKey = @"batteryLevel";

// BloodPressureData key.
NSString * const BLEDeviceBloodPressureDataFlagsKey = @"flags";
NSString * const BLEDeviceBloodPressureDataUnitKey = @"unit";
NSString * const BLEDeviceBloodPressureDataSystolicKey = @"systolic";
NSString * const BLEDeviceBloodPressureDataDiastolicKey = @"diastolic";
NSString * const BLEDeviceBloodPressureDataMeanArterialPressureKey = @"meanArterialPressure";
NSString * const BLEDeviceBloodPressureDataPulseRateKey = @"pulseRate";
NSString * const BLEDeviceBloodPressureDataMeasurementStatusKey = @"measurementStatus";
NSString * const BLEDeviceBloodPressureDataTimeStampKey = @"timeStamp";
NSString * const BLEDeviceBloodPressureDataUserIDKey = @"userID";

// BloodPressureFeature key.
NSString * const BLEDeviceBloodPressureFeatureKey = @"bloodPressureFeature";

// WeightData key.
NSString * const BLEDeviceWeightDataFlagsKey = @"flags";
NSString * const BLEDeviceWeightDataWeightUnitKey = @"weightUnit";
NSString * const BLEDeviceWeightDataHeightUnitKey = @"heightUnit";
NSString * const BLEDeviceWeightDataWeightKey = @"weight";
NSString * const BLEDeviceWeightDataHeightKey = @"height";
NSString * const BLEDeviceWeightDataBMIKey = @"BMI";
NSString * const BLEDeviceWeightDataTimeStampKey = @"timeStamp";
NSString * const BLEDeviceWeightDataUserIDKey = @"userID";

// WeightFeature key.
NSString * const BLEDeviceWeightFeatureKey = @"weightFeature";

// WeightMeasurement definition
const float BLEDeviceWeightSIMagnification = 0.005;
const float BLEDeviceWeightInperialMagnification = 0.01;
