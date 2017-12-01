//
//  GattUUID.java
//
//  Copyright (c) 2016 OMRON HEALTHCARE Co.,Ltd. All rights reserved.
//

package jp.co.omron.healthcare.samplelibs.ble.blenativewrapper;

import java.util.UUID;

public class GattUUID {

    public enum Service {

        // https://developer.bluetooth.org/gatt/services/Pages/ServicesHome.aspx
        AlertNotificationService(UUID.fromString("00001811-0000-1000-8000-00805f9b34fb")),
        AutomationIOService(UUID.fromString("00001815-0000-1000-8000-00805f9b34fb")),
        BatteryService(UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb")),
        BloodPressureService(UUID.fromString("00001810-0000-1000-8000-00805f9b34fb")),
        BodyCompositionService(UUID.fromString("0000181b-0000-1000-8000-00805f9b34fb")),
        BondManagementService(UUID.fromString("0000181e-0000-1000-8000-00805f9b34fb")),
        ContinuousGlucoseMonitoringService(UUID.fromString("0000181f-0000-1000-8000-00805f9b34fb")),
        CurrentTimeService(UUID.fromString("00001805-0000-1000-8000-00805f9b34fb")),
        CyclingPowerService(UUID.fromString("00001818-0000-1000-8000-00805f9b34fb")),
        CyclingSpeedandCadenceService(UUID.fromString("00001816-0000-1000-8000-00805f9b34fb")),
        DeviceInformationService(UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb")),
        EnvironmentalSensingService(UUID.fromString("0000181a-0000-1000-8000-00805f9b34fb")),
        GenericAccessService(UUID.fromString("00001800-0000-1000-8000-00805f9b34fb")),
        GenericAttributeService(UUID.fromString("00001801-0000-1000-8000-00805f9b34fb")),
        GlucoseService(UUID.fromString("00001808-0000-1000-8000-00805f9b34fb")),
        HealthThermometerService(UUID.fromString("00001809-0000-1000-8000-00805f9b34fb")),
        HeartRateService(UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")),
        HTTPProxyService(UUID.fromString("00001823-0000-1000-8000-00805f9b34fb")),
        HumanInterfaceDeviceService(UUID.fromString("00001812-0000-1000-8000-00805f9b34fb")),
        ImmediateAlertService(UUID.fromString("00001802-0000-1000-8000-00805f9b34fb")),
        IndoorPositioningService(UUID.fromString("00001821-0000-1000-8000-00805f9b34fb")),
        InternetProtocolSupportService(UUID.fromString("00001820-0000-1000-8000-00805f9b34fb")),
        LinkLossService(UUID.fromString("00001803-0000-1000-8000-00805f9b34fb")),
        LocationandNavigationService(UUID.fromString("00001819-0000-1000-8000-00805f9b34fb")),
        NextDSTChangeService(UUID.fromString("00001807-0000-1000-8000-00805f9b34fb")),
        ObjectTransferService(UUID.fromString("00001825-0000-1000-8000-00805f9b34fb")),
        PhoneAlertStatusService(UUID.fromString("0000180e-0000-1000-8000-00805f9b34fb")),
        PulseOximeterService(UUID.fromString("00001822-0000-1000-8000-00805f9b34fb")),
        ReferenceTimeUpdateService(UUID.fromString("00001806-0000-1000-8000-00805f9b34fb")),
        RunningSpeedandCadenceService(UUID.fromString("00001814-0000-1000-8000-00805f9b34fb")),
        ScanParametersService(UUID.fromString("00001813-0000-1000-8000-00805f9b34fb")),
        TransportDiscoveryService(UUID.fromString("00001824-0000-1000-8000-00805f9b34fb")),
        TxPowerService(UUID.fromString("00001804-0000-1000-8000-00805f9b34fb")),
        UserDataService(UUID.fromString("0000181c-0000-1000-8000-00805f9b34fb")),
        WeightScaleService(UUID.fromString("0000181d-0000-1000-8000-00805f9b34fb")),
        UnknownService(UUID.fromString("00000000-0000-0000-0000-000000000000"));

        private UUID mUuid;

        Service(UUID uuid) {
            mUuid = uuid;
        }

        public static Service valueOf(UUID uuid) {
            for (Service type : values()) {
                if (type.getUuid().equals(uuid)) {
                    return type;
                }
            }
            return UnknownService;
        }

        public UUID getUuid() {
            return mUuid;
        }
    }

    public enum Characteristic {

        // https://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicsHome.aspx
        AerobicHeartRateLowerLimitCharacteristic(UUID.fromString("00002a7e-0000-1000-8000-00805f9b34fb")),
        AerobicHeartRateUpperLimitCharacteristic(UUID.fromString("00002a84-0000-1000-8000-00805f9b34fb")),
        AerobicThresholdCharacteristic(UUID.fromString("00002a7f-0000-1000-8000-00805f9b34fb")),
        AgeCharacteristic(UUID.fromString("00002a80-0000-1000-8000-00805f9b34fb")),
        AggregateCharacteristic(UUID.fromString("00002a5a-0000-1000-8000-00805f9b34fb")),
        AlertCategoryIDCharacteristic(UUID.fromString("00002a43-0000-1000-8000-00805f9b34fb")),
        AlertCategoryIDBitMaskCharacteristic(UUID.fromString("00002a42-0000-1000-8000-00805f9b34fb")),
        AlertLevelCharacteristic(UUID.fromString("00002a06-0000-1000-8000-00805f9b34fb")),
        AlertNotificationControlPointCharacteristic(UUID.fromString("00002a44-0000-1000-8000-00805f9b34fb")),
        AlertStatusCharacteristic(UUID.fromString("00002a3f-0000-1000-8000-00805f9b34fb")),
        AltitudeCharacteristic(UUID.fromString("00002ab3-0000-1000-8000-00805f9b34fb")),
        AnaerobicHeartRateLowerLimitCharacteristic(UUID.fromString("00002a81-0000-1000-8000-00805f9b34fb")),
        AnaerobicHeartRateUpperLimitCharacteristic(UUID.fromString("00002a82-0000-1000-8000-00805f9b34fb")),
        AnaerobicThresholdCharacteristic(UUID.fromString("00002a83-0000-1000-8000-00805f9b34fb")),
        AnalogCharacteristic(UUID.fromString("00002a58-0000-1000-8000-00805f9b34fb")),
        ApparentWindDirectionCharacteristic(UUID.fromString("00002a73-0000-1000-8000-00805f9b34fb")),
        ApparentWindSpeedCharacteristic(UUID.fromString("00002a72-0000-1000-8000-00805f9b34fb")),
        AppearanceCharacteristic(UUID.fromString("00002a01-0000-1000-8000-00805f9b34fb")),
        BarometricPressureTrendCharacteristic(UUID.fromString("00002aa3-0000-1000-8000-00805f9b34fb")),
        BatteryLevelCharacteristic(UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb")),
        BloodPressureFeatureCharacteristic(UUID.fromString("00002a49-0000-1000-8000-00805f9b34fb")),
        BloodPressureMeasurementCharacteristic(UUID.fromString("00002a35-0000-1000-8000-00805f9b34fb")),
        BodyCompositionFeatureCharacteristic(UUID.fromString("00002a9b-0000-1000-8000-00805f9b34fb")),
        BodyCompositionMeasurementCharacteristic(UUID.fromString("00002a9c-0000-1000-8000-00805f9b34fb")),
        BodySensorLocationCharacteristic(UUID.fromString("00002a38-0000-1000-8000-00805f9b34fb")),
        BondManagementControlPointCharacteristic(UUID.fromString("00002aa4-0000-1000-8000-00805f9b34fb")),
        BondManagementFeatureCharacteristic(UUID.fromString("00002aa5-0000-1000-8000-00805f9b34fb")),
        BootKeyboardInputReportCharacteristic(UUID.fromString("00002a22-0000-1000-8000-00805f9b34fb")),
        BootKeyboardOutputReportCharacteristic(UUID.fromString("00002a32-0000-1000-8000-00805f9b34fb")),
        BootMouseInputReportCharacteristic(UUID.fromString("00002a33-0000-1000-8000-00805f9b34fb")),
        CentralAddressResolutionCharacteristic(UUID.fromString("00002aa6-0000-1000-8000-00805f9b34fb")),
        CGMFeatureCharacteristic(UUID.fromString("00002aa8-0000-1000-8000-00805f9b34fb")),
        CGMMeasurementCharacteristic(UUID.fromString("00002aa7-0000-1000-8000-00805f9b34fb")),
        CGMSessionRunTimeCharacteristic(UUID.fromString("00002aab-0000-1000-8000-00805f9b34fb")),
        CGMSessionStartTimeCharacteristic(UUID.fromString("00002aaa-0000-1000-8000-00805f9b34fb")),
        CGMSpecificOpsControlPointCharacteristic(UUID.fromString("00002aac-0000-1000-8000-00805f9b34fb")),
        CGMStatusCharacteristic(UUID.fromString("00002aa9-0000-1000-8000-00805f9b34fb")),
        CSCFeatureCharacteristic(UUID.fromString("00002a5c-0000-1000-8000-00805f9b34fb")),
        CSCMeasurementCharacteristic(UUID.fromString("00002a5b-0000-1000-8000-00805f9b34fb")),
        CurrentTimeCharacteristic(UUID.fromString("00002a2b-0000-1000-8000-00805f9b34fb")),
        CyclingPowerControlPointCharacteristic(UUID.fromString("00002a66-0000-1000-8000-00805f9b34fb")),
        CyclingPowerFeatureCharacteristic(UUID.fromString("00002a65-0000-1000-8000-00805f9b34fb")),
        CyclingPowerMeasurementCharacteristic(UUID.fromString("00002a63-0000-1000-8000-00805f9b34fb")),
        CyclingPowerVectorCharacteristic(UUID.fromString("00002a64-0000-1000-8000-00805f9b34fb")),
        DatabaseChangeIncrementCharacteristic(UUID.fromString("00002a99-0000-1000-8000-00805f9b34fb")),
        DateOfBirthCharacteristic(UUID.fromString("00002a85-0000-1000-8000-00805f9b34fb")),
        DateOfThresholdAssessmentCharacteristic(UUID.fromString("00002a86-0000-1000-8000-00805f9b34fb")),
        DateTimeCharacteristic(UUID.fromString("00002a08-0000-1000-8000-00805f9b34fb")),
        DayDateTimeCharacteristic(UUID.fromString("00002a0a-0000-1000-8000-00805f9b34fb")),
        DayOfWeekCharacteristic(UUID.fromString("00002a09-0000-1000-8000-00805f9b34fb")),
        DescriptorValueChangedCharacteristic(UUID.fromString("00002a7d-0000-1000-8000-00805f9b34fb")),
        DeviceNameCharacteristic(UUID.fromString("00002a00-0000-1000-8000-00805f9b34fb")),
        DewPointCharacteristic(UUID.fromString("00002a7b-0000-1000-8000-00805f9b34fb")),
        DigitalCharacteristic(UUID.fromString("00002a56-0000-1000-8000-00805f9b34fb")),
        DSTOffsetCharacteristic(UUID.fromString("00002a0d-0000-1000-8000-00805f9b34fb")),
        ElevationCharacteristic(UUID.fromString("00002a6c-0000-1000-8000-00805f9b34fb")),
        EmailAddressCharacteristic(UUID.fromString("00002a87-0000-1000-8000-00805f9b34fb")),
        ExactTime256Characteristic(UUID.fromString("00002a0c-0000-1000-8000-00805f9b34fb")),
        FatBurnHeartRateLowerLimitCharacteristic(UUID.fromString("00002a88-0000-1000-8000-00805f9b34fb")),
        FatBurnHeartRateUpperLimitCharacteristic(UUID.fromString("00002a89-0000-1000-8000-00805f9b34fb")),
        FirmwareRevisionStringCharacteristic(UUID.fromString("00002a26-0000-1000-8000-00805f9b34fb")),
        FirstNameCharacteristic(UUID.fromString("00002a8a-0000-1000-8000-00805f9b34fb")),
        FiveZoneHeartRateLimitsCharacteristic(UUID.fromString("00002a8b-0000-1000-8000-00805f9b34fb")),
        FloorNumberCharacteristic(UUID.fromString("00002ab2-0000-1000-8000-00805f9b34fb")),
        GenderCharacteristic(UUID.fromString("00002a8c-0000-1000-8000-00805f9b34fb")),
        GlucoseFeatureCharacteristic(UUID.fromString("00002a51-0000-1000-8000-00805f9b34fb")),
        GlucoseMeasurementCharacteristic(UUID.fromString("00002a18-0000-1000-8000-00805f9b34fb")),
        GlucoseMeasurementContextCharacteristic(UUID.fromString("00002a34-0000-1000-8000-00805f9b34fb")),
        GustFactorCharacteristic(UUID.fromString("00002a74-0000-1000-8000-00805f9b34fb")),
        HardwareRevisionStringCharacteristic(UUID.fromString("00002a27-0000-1000-8000-00805f9b34fb")),
        HeartRateControlPointCharacteristic(UUID.fromString("00002a39-0000-1000-8000-00805f9b34fb")),
        HeartRateMaxCharacteristic(UUID.fromString("00002a8d-0000-1000-8000-00805f9b34fb")),
        HeartRateMeasurementCharacteristic(UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")),
        HeatIndexCharacteristic(UUID.fromString("00002a7a-0000-1000-8000-00805f9b34fb")),
        HeightCharacteristic(UUID.fromString("00002a8e-0000-1000-8000-00805f9b34fb")),
        HIDControlPointCharacteristic(UUID.fromString("00002a4c-0000-1000-8000-00805f9b34fb")),
        HIDInformationCharacteristic(UUID.fromString("00002a4a-0000-1000-8000-00805f9b34fb")),
        HipCircumferenceCharacteristic(UUID.fromString("00002a8f-0000-1000-8000-00805f9b34fb")),
        HTTPControlPointCharacteristic(UUID.fromString("00002aba-0000-1000-8000-00805f9b34fb")),
        HTTPEntityBodyCharacteristic(UUID.fromString("00002ab9-0000-1000-8000-00805f9b34fb")),
        HTTPHeadersCharacteristic(UUID.fromString("00002ab7-0000-1000-8000-00805f9b34fb")),
        HTTPStatusCodeCharacteristic(UUID.fromString("00002ab8-0000-1000-8000-00805f9b34fb")),
        HTTPSSecurityCharacteristic(UUID.fromString("00002abb-0000-1000-8000-00805f9b34fb")),
        HumidityCharacteristic(UUID.fromString("00002a6f-0000-1000-8000-00805f9b34fb")),
        IEEE1107320601RegulatoryCertificationDataListCharacteristic(UUID.fromString("00002a2a-0000-1000-8000-00805f9b34fb")),
        IndoorPositioningConfigurationCharacteristic(UUID.fromString("00002aad-0000-1000-8000-00805f9b34fb")),
        IntermediateCuffPressureCharacteristic(UUID.fromString("00002a36-0000-1000-8000-00805f9b34fb")),
        IntermediateTemperatureCharacteristic(UUID.fromString("00002a1e-0000-1000-8000-00805f9b34fb")),
        IrradianceCharacteristic(UUID.fromString("00002a77-0000-1000-8000-00805f9b34fb")),
        LanguageCharacteristic(UUID.fromString("00002aa2-0000-1000-8000-00805f9b34fb")),
        LastNameCharacteristic(UUID.fromString("00002a90-0000-1000-8000-00805f9b34fb")),
        LatitudeCharacteristic(UUID.fromString("00002aae-0000-1000-8000-00805f9b34fb")),
        LNControlPointCharacteristic(UUID.fromString("00002a6b-0000-1000-8000-00805f9b34fb")),
        LNFeatureCharacteristic(UUID.fromString("00002a6a-0000-1000-8000-00805f9b34fb")),
        LocalEastCoordinateCharacteristic(UUID.fromString("00002ab1-0000-1000-8000-00805f9b34fb")),
        LocalNorthCoordinateCharacteristic(UUID.fromString("00002ab0-0000-1000-8000-00805f9b34fb")),
        LocalTimeInformationCharacteristic(UUID.fromString("00002a0f-0000-1000-8000-00805f9b34fb")),
        LocationAndSpeedCharacteristic(UUID.fromString("00002a67-0000-1000-8000-00805f9b34fb")),
        LocationNameCharacteristic(UUID.fromString("00002ab5-0000-1000-8000-00805f9b34fb")),
        LongitudeCharacteristic(UUID.fromString("00002aaf-0000-1000-8000-00805f9b34fb")),
        MagneticDeclinationCharacteristic(UUID.fromString("00002a2c-0000-1000-8000-00805f9b34fb")),
        MagneticFluxDensity2DCharacteristic(UUID.fromString("00002aa0-0000-1000-8000-00805f9b34fb")),
        MagneticFluxDensity3DCharacteristic(UUID.fromString("00002aa1-0000-1000-8000-00805f9b34fb")),
        ManufacturerNameStringCharacteristic(UUID.fromString("00002a29-0000-1000-8000-00805f9b34fb")),
        MaximumRecommendedHeartRateCharacteristic(UUID.fromString("00002a91-0000-1000-8000-00805f9b34fb")),
        MeasurementIntervalCharacteristic(UUID.fromString("00002a21-0000-1000-8000-00805f9b34fb")),
        ModelNumberStringCharacteristic(UUID.fromString("00002a24-0000-1000-8000-00805f9b34fb")),
        NavigationCharacteristic(UUID.fromString("00002a68-0000-1000-8000-00805f9b34fb")),
        NewAlertCharacteristic(UUID.fromString("00002a46-0000-1000-8000-00805f9b34fb")),
        ObjectActionControlPointCharacteristic(UUID.fromString("00002ac5-0000-1000-8000-00805f9b34fb")),
        ObjectChangedCharacteristic(UUID.fromString("00002ac8-0000-1000-8000-00805f9b34fb")),
        ObjectFirstCreatedCharacteristic(UUID.fromString("00002ac1-0000-1000-8000-00805f9b34fb")),
        ObjectIDCharacteristic(UUID.fromString("00002ac3-0000-1000-8000-00805f9b34fb")),
        ObjectLastModifiedCharacteristic(UUID.fromString("00002ac2-0000-1000-8000-00805f9b34fb")),
        ObjectListControlPointCharacteristic(UUID.fromString("00002ac6-0000-1000-8000-00805f9b34fb")),
        ObjectListFilterCharacteristic(UUID.fromString("00002ac7-0000-1000-8000-00805f9b34fb")),
        ObjectNameCharacteristic(UUID.fromString("00002abe-0000-1000-8000-00805f9b34fb")),
        ObjectPropertiesCharacteristic(UUID.fromString("00002ac4-0000-1000-8000-00805f9b34fb")),
        ObjectSizeCharacteristic(UUID.fromString("00002ac0-0000-1000-8000-00805f9b34fb")),
        ObjectTypeCharacteristic(UUID.fromString("00002abf-0000-1000-8000-00805f9b34fb")),
        OTSFeatureCharacteristic(UUID.fromString("00002abd-0000-1000-8000-00805f9b34fb")),
        PeripheralPreferredConnectionParametersCharacteristic(UUID.fromString("00002a04-0000-1000-8000-00805f9b34fb")),
        PeripheralPrivacyFlagCharacteristic(UUID.fromString("00002a02-0000-1000-8000-00805f9b34fb")),
        PLXContinuousMeasurementCharacteristic(UUID.fromString("00002a5f-0000-1000-8000-00805f9b34fb")),
        PLXFeaturesCharacteristic(UUID.fromString("00002a60-0000-1000-8000-00805f9b34fb")),
        PLXSpotCheckMeasurementCharacteristic(UUID.fromString("00002a5e-0000-1000-8000-00805f9b34fb")),
        PnPIDCharacteristic(UUID.fromString("00002a50-0000-1000-8000-00805f9b34fb")),
        PollenConcentrationCharacteristic(UUID.fromString("00002a75-0000-1000-8000-00805f9b34fb")),
        PositionQualityCharacteristic(UUID.fromString("00002a69-0000-1000-8000-00805f9b34fb")),
        PressureCharacteristic(UUID.fromString("00002a6d-0000-1000-8000-00805f9b34fb")),
        ProtocolModeCharacteristic(UUID.fromString("00002a4e-0000-1000-8000-00805f9b34fb")),
        RainfallCharacteristic(UUID.fromString("00002a78-0000-1000-8000-00805f9b34fb")),
        ReconnectionAddressCharacteristic(UUID.fromString("00002a03-0000-1000-8000-00805f9b34fb")),
        RecordAccessControlPointCharacteristic(UUID.fromString("00002a52-0000-1000-8000-00805f9b34fb")),
        ReferenceTimeInformationCharacteristic(UUID.fromString("00002a14-0000-1000-8000-00805f9b34fb")),
        ReportCharacteristic(UUID.fromString("00002a4d-0000-1000-8000-00805f9b34fb")),
        ReportMapCharacteristic(UUID.fromString("00002a4b-0000-1000-8000-00805f9b34fb")),
        RestingHeartRateCharacteristic(UUID.fromString("00002a92-0000-1000-8000-00805f9b34fb")),
        RingerControlPointCharacteristic(UUID.fromString("00002a40-0000-1000-8000-00805f9b34fb")),
        RingerSettingCharacteristic(UUID.fromString("00002a41-0000-1000-8000-00805f9b34fb")),
        RSCFeatureCharacteristic(UUID.fromString("00002a54-0000-1000-8000-00805f9b34fb")),
        RSCMeasurementCharacteristic(UUID.fromString("00002a53-0000-1000-8000-00805f9b34fb")),
        SCControlPointCharacteristic(UUID.fromString("00002a55-0000-1000-8000-00805f9b34fb")),
        ScanIntervalWindowCharacteristic(UUID.fromString("00002a4f-0000-1000-8000-00805f9b34fb")),
        ScanRefreshCharacteristic(UUID.fromString("00002a31-0000-1000-8000-00805f9b34fb")),
        SensorLocationCharacteristic(UUID.fromString("00002a5d-0000-1000-8000-00805f9b34fb")),
        SerialNumberStringCharacteristic(UUID.fromString("00002a25-0000-1000-8000-00805f9b34fb")),
        ServiceChangedCharacteristic(UUID.fromString("00002a05-0000-1000-8000-00805f9b34fb")),
        SoftwareRevisionStringCharacteristic(UUID.fromString("00002a28-0000-1000-8000-00805f9b34fb")),
        SportTypeForAerobicAndAnaerobicThresholdsCharacteristic(UUID.fromString("00002a93-0000-1000-8000-00805f9b34fb")),
        SupportedNewAlertCategoryCharacteristic(UUID.fromString("00002a47-0000-1000-8000-00805f9b34fb")),
        SupportedUnreadAlertCategoryCharacteristic(UUID.fromString("00002a48-0000-1000-8000-00805f9b34fb")),
        SystemIDCharacteristic(UUID.fromString("00002a23-0000-1000-8000-00805f9b34fb")),
        TDSControlPointCharacteristic(UUID.fromString("00002abc-0000-1000-8000-00805f9b34fb")),
        TemperatureCharacteristic(UUID.fromString("00002a6e-0000-1000-8000-00805f9b34fb")),
        TemperatureMeasurementCharacteristic(UUID.fromString("00002a1c-0000-1000-8000-00805f9b34fb")),
        TemperatureTypeCharacteristic(UUID.fromString("00002a1d-0000-1000-8000-00805f9b34fb")),
        ThreeZoneHeartRateLimitsCharacteristic(UUID.fromString("00002a94-0000-1000-8000-00805f9b34fb")),
        TimeAccuracyCharacteristic(UUID.fromString("00002a12-0000-1000-8000-00805f9b34fb")),
        TimeSourceCharacteristic(UUID.fromString("00002a13-0000-1000-8000-00805f9b34fb")),
        TimeUpdateControlPointCharacteristic(UUID.fromString("00002a16-0000-1000-8000-00805f9b34fb")),
        TimeUpdateStateCharacteristic(UUID.fromString("00002a17-0000-1000-8000-00805f9b34fb")),
        TimeWithDSTCharacteristic(UUID.fromString("00002a11-0000-1000-8000-00805f9b34fb")),
        TimeZoneCharacteristic(UUID.fromString("00002a0e-0000-1000-8000-00805f9b34fb")),
        TrueWindDirectionCharacteristic(UUID.fromString("00002a71-0000-1000-8000-00805f9b34fb")),
        TrueWindSpeedCharacteristic(UUID.fromString("00002a70-0000-1000-8000-00805f9b34fb")),
        TwoZoneHeartRateLimitCharacteristic(UUID.fromString("00002a95-0000-1000-8000-00805f9b34fb")),
        TxPowerLevelCharacteristic(UUID.fromString("00002a07-0000-1000-8000-00805f9b34fb")),
        UncertaintyCharacteristic(UUID.fromString("00002ab4-0000-1000-8000-00805f9b34fb")),
        UnreadAlertStatusCharacteristic(UUID.fromString("00002a45-0000-1000-8000-00805f9b34fb")),
        URICharacteristic(UUID.fromString("00002ab6-0000-1000-8000-00805f9b34fb")),
        UserControlPointCharacteristic(UUID.fromString("00002a9f-0000-1000-8000-00805f9b34fb")),
        UserIndexCharacteristic(UUID.fromString("00002a9a-0000-1000-8000-00805f9b34fb")),
        UVIndexCharacteristic(UUID.fromString("00002a76-0000-1000-8000-00805f9b34fb")),
        VO2MaxCharacteristic(UUID.fromString("00002a96-0000-1000-8000-00805f9b34fb")),
        WaistCircumferenceCharacteristic(UUID.fromString("00002a97-0000-1000-8000-00805f9b34fb")),
        WeightCharacteristic(UUID.fromString("00002a98-0000-1000-8000-00805f9b34fb")),
        WeightMeasurementCharacteristic(UUID.fromString("00002a9d-0000-1000-8000-00805f9b34fb")),
        WeightScaleFeatureCharacteristic(UUID.fromString("00002a9e-0000-1000-8000-00805f9b34fb")),
        WindChillCharacteristic(UUID.fromString("00002a79-0000-1000-8000-00805f9b34fb")),
        UnknownCharacteristic(UUID.fromString("00000000-0000-0000-0000-000000000000"));

        private UUID mUuid;

        Characteristic(UUID uuid) {
            mUuid = uuid;
        }

        public static Characteristic valueOf(UUID uuid) {
            for (Characteristic type : values()) {
                if (type.getUuid().equals(uuid)) {
                    return type;
                }
            }
            return UnknownCharacteristic;
        }

        public UUID getUuid() {
            return mUuid;
        }
    }

    public enum Descriptor {

        // https://developer.bluetooth.org/gatt/descriptors/Pages/DescriptorsHomePage.aspx
        CharacteristicExtendedPropertiesDescriptor(UUID.fromString("00002900-0000-1000-8000-00805f9b34fb")),
        CharacteristicUserDescriptionDescriptor(UUID.fromString("00002901-0000-1000-8000-00805f9b34fb")),
        ClientCharacteristicConfigurationDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")),
        ServerCharacteristicConfigurationDescriptor(UUID.fromString("00002903-0000-1000-8000-00805f9b34fb")),
        CharacteristicPresentationFormatDescriptor(UUID.fromString("00002904-0000-1000-8000-00805f9b34fb")),
        CharacteristicAggregateFormatDescriptor(UUID.fromString("00002905-0000-1000-8000-00805f9b34fb")),
        ValidRangeDescriptor(UUID.fromString("00002906-0000-1000-8000-00805f9b34fb")),
        ExternalReportReferenceDescriptor(UUID.fromString("00002907-0000-1000-8000-00805f9b34fb")),
        ReportReferenceDescriptor(UUID.fromString("00002908-0000-1000-8000-00805f9b34fb")),
        NumberOfDigitalsDescriptor(UUID.fromString("00002909-0000-1000-8000-00805f9b34fb")),
        ValueTriggerSettingDescriptor(UUID.fromString("0000290a-0000-1000-8000-00805f9b34fb")),
        EnvironmentalSensingConfigurationDescriptor(UUID.fromString("0000290b-0000-1000-8000-00805f9b34fb")),
        EnvironmentalSensingMeasurementDescriptor(UUID.fromString("0000290c-0000-1000-8000-00805f9b34fb")),
        EnvironmentalSensingTriggerSettingDescriptor(UUID.fromString("0000290d-0000-1000-8000-00805f9b34fb")),
        TimeTriggerSettingDescriptor(UUID.fromString("0000290e-0000-1000-8000-00805f9b34fb")),
        UnknownDescriptor(UUID.fromString("00000000-0000-0000-0000-000000000000"));

        private UUID mUuid;

        Descriptor(UUID uuid) {
            mUuid = uuid;
        }

        public static Descriptor valueOf(UUID uuid) {
            for (Descriptor type : values()) {
                if (type.getUuid().equals(uuid)) {
                    return type;
                }
            }
            return UnknownDescriptor;
        }

        public UUID getUuid() {
            return mUuid;
        }
    }
}
