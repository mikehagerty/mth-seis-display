package edu.sc.seis.seisFile.stationxml;


public final class StationXMLTagNames {
    
    public static final String[] OLD_SCHEMA_VERSIONS = {"http://www.data.scec.org/xml/station/20111019/"};
    public static final String[] OLD_SCHEMA_SEISFILE_VERSIONS = {"1.3.1"};
    
    public static final String SCHEMA_VERSION = "http://www.data.scec.org/xml/station/20120307/";
    
    private StationXMLTagNames() {}
    

    public static final String NAMESPACE = "http://www.data.scec.org/xml/station/";
    public static final String STAMESSAGE = "StaMessage";
    public static final String SOURCE = "Source";
    public static final String SENDER = "Sender";
    public static final String MODULE = "Module";
    public static final String SENTDATE = "SentDate";
    public static final String NETWORK = "Network";
    public static final String TOTALNUMSTATIONS = "TotalNumberStations";
    public static final String SELECTEDNUMSTATIONS = "SelectedNumberStations";
    public static final String STATION = "Station";
    public static final String STATION_EPOCH = "StationEpoch";
    public static final String CHANNEL = "Channel";
    public static final String NET_CODE = "net_code";
    public static final String STA_CODE = "sta_code";
    public static final String SITE = "Site";
    public static final String LOC_CODE = "loc_code";
    public static final String CHAN_CODE = "chan_code";
    public static final String DESCRIPTION = "Description";
    public static final String EPOCH = "Epoch";


    public static final String TOWN = "Town";
    public static final String COUNTY = "County";
    public static final String STATE = "State";
    public static final String COUNTRY = "Country";
    public static final String STARTDATE = "StartDate";
    public static final String ENDDATE = "EndDate";
    public static final String LAT = "Lat";
    public static final String LON = "Lon";
    public static final String ELEVATION = "Elevation";
    public static final String NAME = "Name";
    public static final String CREATIONDATE = "CreationDate";
    public static final String TOTALNUMCHANNELS = "TotalNumberChannels";
    public static final String SELECTEDNUMCHANNELS = "SelectedNumberChannels";
    public static final String SENSITIVITY_VALUE = "SensitivityValue";
    public static final String FREQUENCY = "Frequency";
    public static final String SENSITIVITY_UNITS = "SensitivityUnits";

    public static final String DEPTH = "Depth";
    public static final String AZIMUTH = "Azimuth";
    public static final String DIP = "Dip";
    public static final String SAMPLE_RATE = "SampleRate";
    public static final String CLOCK_DRIFT = "ClockDrift";
    public static final String SENSOR = "Sensor";
    public static final String INSTRUMENT_SENSITIVITY = "InstrumentSensitivity";
    public static final String EQUIP_TYPE = "EquipType";

    public static final String RESPONSE = "Response";
    public static final String POLESZEROS = "PolesZeros";
    public static final String INPUTUNITS = "InputUnits";
    public static final String OUTPUTUNITS = "OutputUnits";
    public static final String PZTRANSFERTYPE = "PzTransferFunctionType";
    public static final String CFTRANSFERTYPE = "CfTransferFunctionType";
    public static final String NORMALIZATIONFACTOR = "NormalizationFactor";
    public static final String NORMALIZATIONFREQ = "NormalizationFreq";
    public static final String POLE = "Pole";
    public static final String ZERO = "Zero";
    public static final String REAL = "Real";
    public static final String IMAGINARY = "Imaginary";
    public static final String STAGESENSITIVITY = "StageSensitivity";
    public static final String SENSITIVITYVALUE = "SensitivityValue";
    public static final String SENSITIVITYUNITS = "SensitivityUnits";
    @Deprecated
    public static final String GAINUNITS = "GainUnits"; // no longer used
    public static final String DECIMATION = "Decimation";
    public static final String INPUTSAMPLERATE = "InputSampleRate";
    public static final String FACTOR = "Factor";
    public static final String OFFSET = "Offset";
    public static final String CORRECTION = "Correction";
    public static final String COEFFICIENTS = "Coefficients";
    public static final String NUMERATOR = "Numerator";
    public static final String DENOMINATOR = "Demoninator";
    public static final String STAGE = "stage";
    public static final String STAGEDESCRIPTION = "stage_description";
    public static final String RESPONSELIST = "ResponseList";
    public static final String GENERIC = "Generic";
    public static final String FIR = "FIR";
    public static final String POLYNOMIAL = "Polynomial";
    public static final String SPECTRA = "Spectra";
    public static final String COMMENT = "Comment";
    public static final String DELAY = "Delay";
    public static final String RESPONSELISTELEMENT = "ResponseListElement";
    public static final String AMPLITUDE = "Amplitude";
    public static final String PHASE = "Phase";
    public static final String RESPONSENAME = "ResponseName";
    public static final String SYMMETRY = "Symmetry";
    public static final String NUMERATORCOEFFICIENT = "NumeratorCoefficient";
    public static final String COEFFICIENT = "Coefficient";
    public static final String APPROXIMATIONTYPE = "ApproximationType";
    public static final String FREQLOWERBOUND = "FreqLowerBound";
    public static final String FREQUPPERBOUND = "FreqUpperBound";
    public static final String APPROXLOWERBOUND = "ApproxLowerBound";
    public static final String APPROXUPPERBOUND = "ApproxUpperBound";
    public static final String MAXERROR = "MaxError";
    public static final String GENCOMMENT = "GenComment";
    public static final String SENSITIVITY = "Sensitivity";
    public static final String FREEFREQ = "FreeFreq";
    public static final String HIGHPASS = "HighPass";
    public static final String LOWPASS = "LowPass";
    public static final String MANUFACTURER = "Manufacturer";
    public static final String VENDOR = "Vendor";
    public static final String MODEL = "Model";
    public static final String SERIALNUMBER = "SerialNumber";
    public static final String INSTALLATIONDATE = "InstallationDate";
    public static final String REMOVALDATE = "RemovalDate";
    public static final String CALIBRATIONDATE = "CalibrationDate";
    public static final String TOTALCHANNELS = "TotalChannels";
    public static final String RECORDEDCHANNELS = "RecordedChannels";
    public static final String DATALOGGER = "DataLogger";
    public static final String XMLNS = "xmlns";
    
    public static final String IRISSTATIONCOMMENTS = "StationComments";
    public static final String IRISCHANNELCOMMENTS = "ChannelComments";
    public static final String IRISCOMMENT = "Comment";
    public static final String IRISTEXT = "Text";
    public static final String IRISCLASS = "Class";
    }
