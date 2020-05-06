package com.example.powermonitor;

import android.content.Context;
import android.util.Log;

import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;

import java.util.Arrays;

public class CFT232h
{
    public enum FT_STATUS
    {
        FT_OK ,
        FT_DEVICE_NOT_OPENED,
        FT_DEVICE_NOT_FOUND ,
        FT_FAIL_TO_WRITE_DEVICE,
        FT_IO_ERROR
    }

    public static class DEF_FT232H
    {
        public static final int CMD_MPSSE_DATA_OUT_BITS_POS_EDGE	    = 0x12;
        public static final int CMD_MPSSE_DATA_OUT_BITS_NEG_EDGE	    = 0x13;
        public static final int CMD_SET_LOW_BYTE_DATA_BITS_DATA         = 0x13;
        public static final int CMD_MPSSE_DATA_IN_BITS_POS_EDGE		    = 0x22;
        public static final int CMD_MPSSE_DATA_IN_BITS_NEG_EDGE		    = 0x26;
        public static final int CMD_MPSSE_DATA_BITS_IN_POS_OUT_NEG_EDGE	= 0x33;
        public static final int CMD_MPSSE_DATA_BITS_IN_NEG_OUT_POS_EDGE	= 0x36;
        public static final int CMD_SET_LOW_BYTE_DATA_BITS              = 0x80;
        public static final int CMD_GET_LOW_BYTE_DATA_BITS              = 0x81;
        public static final int CMD_SET_HIGH_BYTE_DATA_BITS             = 0x82;
        public static final int CMD_GET_HIGH_BYTE_DATA_BITS             = 0x83;
        public static final int CMD_TURN_ON_LOOPBACK_CMD                = 0x84;
        public static final int CMD_TURN_OFF_LOOPBACK_CMD               = 0x85;
        public static final int CMD_MID_SET_CLOCK_FREQUENCY             = 0x86;
        public static final int CMD_MPSSE_SEND_IMMEDIATE                = 0x87;
        public static final int CMD_DISABLE_CLOCK_DIVIDE	            = 0x8A;
        public static final int CMD_ENABLE_CLOCK_DIVIDE	                = 0x8B;
        public static final int CMD_ENABLE_3PHASE_CLOCKING	            = 0x8C;
        public static final int CMD_DISABLE_3PHASE_CLOCKING             = 0x8D;
        public static final int CMD_MPSSE_ENABLE_DRIVE_ONLY_ZERO	    = 0x9E;

        public static final int I2C_TRANSFER_OPTIONS_START_BIT          = 0x01;
        public static final int I2C_TRANSFER_OPTIONS_STOP_BIT           = 0x02;
        public static final int I2C_TRANSFER_OPTIONS_BREAK_ON_NACK      = 0x04;
        public static final int I2C_TRANSFER_OPTIONS_NACK_LAST_BYTE     = 0x08;
        public static final int I2C_TRANSFER_OPTIONS_FAST_TRANSFER      = 0x30;

        public static final int CLOCK_100KHZ                            = 100000;
        public static final int CLOCK_400KHZ                            = 400000;
        public static final int MID_6MHZ                                = 6000000;
        public static final int MID_30MHZ                               = 30000000;

        public static final int ADC_CONVERSION_REG                      = 0x0;
        public static final int ADC_CONFIGURATION_REG                   = 0x1;
        public static final int ADC_SLAVE_ADDRESS                       = 0x48;     //b01001000 -- GND
                                                                                    //b01001001 -- VDD
                                                                                    //b01001011 -- SCL
        /*SCL & SDA directions*/
        public static final int DIRECTION_SCLIN_SDAIN				    = 0x10;
        public static final int DIRECTION_SCLOUT_SDAIN				    = 0x11;
        public static final int DIRECTION_SCLIN_SDAOUT				    = 0x12;
        public static final int DIRECTION_SCLOUT_SDAOUT				    = 0x13;

        /*SCL & SDA values*/
        public static final int VALUE_SCLLOW_SDALOW					    = 0x00;
        public static final int VALUE_SCLHIGH_SDALOW				    = 0x01;
        public static final int VALUE_SCLLOW_SDAHIGH				    = 0x02;
        public static final int VALUE_SCLHIGH_SDAHIGH				    = 0x03;
        public static final int START_DURATION_1	                    = 10;
        public static final int START_DURATION_2	                    = 20;
        public static final int STOP_DURATION_1 	                    = 10;
        public static final int STOP_DURATION_2 	                    = 10;
        public static final int STOP_DURATION_3 	                    = 10;
        public static final int SEND_ACK			                    = 0x00;
        public static final int SEND_NACK			                    = 0x80;
        public static final int I2C_ADDRESS_READ_MASK	                = 0x01;	/*LSB 1 = Read*/
        public static final int I2C_ADDRESS_WRITE_MASK	                = 0xFE;	/*LSB 0 = Write*/
        public static final int DATA_SIZE_8BITS      	                = 0x07;
        public static final int DATA_SIZE_1BIT      	                = 0x00;

    }

    Context OpenDeviceFragmentContext;
    D2xxManager.DriverParameters m_d2xxDrvParameter;
    D2xxManager.FtDeviceInfoListNode m_DeviceList;
    public D2xxManager m_ftD2xx;
    public FT_Device m_ftDevice = null;

    public CFT232h(Context parentContext)
    {
        OpenDeviceFragmentContext = parentContext;
        Ft232_CreateInstance();
    }

    public void uSleep( int microsecond )
    {
        microsecond *= 1000;
        long start = System.nanoTime();
        long end=0;
        do{
            end = System.nanoTime();
        }while(start + microsecond >= end);
    }

    public void mSleep( int milisecond )
    {
        milisecond *= 1000000;
        long start = System.nanoTime();
        long end=0;
        do{
            end = System.nanoTime();
        }while(start + milisecond >= end);
    }

    public void Ft232_CreateInstance()
    {
        try {
            m_ftD2xx = D2xxManager.getInstance(OpenDeviceFragmentContext);
        } catch (D2xxManager.D2xxException ex) {
            ex.printStackTrace();
        }
        m_d2xxDrvParameter  = new D2xxManager.DriverParameters();
    }

    public D2xxManager.FtDeviceInfoListNode Ft232_OpenDevice()
    {
        boolean bOpen = false;
        int devCount = 0;

        if( m_ftD2xx == null ) return null;

        devCount = m_ftD2xx.createDeviceInfoList(OpenDeviceFragmentContext);
        Log.i("Misc Function Test ","Device number = " + Integer.toString(devCount));

        if (devCount > 0)
        {
            m_DeviceList = m_ftD2xx.getDeviceInfoListDetail(0);

            // openByIndex
            m_ftDevice = m_ftD2xx.openByIndex(OpenDeviceFragmentContext, 0, m_d2xxDrvParameter);
            if ( !m_ftDevice.isOpen() ) {
                Log.e("FT232H","device open error");
            }else{
                Log.i("FT232H", String.format("device open success [desc:%s]", m_DeviceList.description));
                bOpen =  true;
            }
        }
        if( bOpen ) return m_DeviceList;
        else        return null;
    }

    public boolean Ft232_CloseDevice()
    {
        if( m_ftDevice == null ) return false;
        if( m_ftDevice.isOpen() )
        {
            m_ftDevice.close();
            m_ftDevice = null;
        }
        return true;
    }

    public boolean Ft232_IsOpenDevice()
    {
        if( m_ftDevice == null ) return false;

        return m_ftDevice.isOpen();
    }

    public boolean Ft232_ResetDevice()
    {
        if( m_ftD2xx == null ) return false;

        boolean ok = m_ftDevice.resetDevice();
        mSleep(1);
        return ok;
    }

    /*
        ftStatus |= FT_SetUSBParameters(ftHandle, 65536, 65535); // Set USB request transfer sizes
        ftStatus |= FT_SetChars(ftHandle, false, 0, false, 0); // Disable event/error characters
        ftStatus |= FT_SetTimeouts(ftHandle, 5000, 5000); // Set rd/wr timeouts to 5 sec
        ftStatus |= FT_SetLatencyTimer(ftHandle, 16); // Latency timer at default 16ms
        ftStatus |= FT_SetBitMode(ftHandle, 0x0, 0x00); // Reset mode to setting in EEPROM

        Buffer Num : 16
        Buffer Size : 16384(16K)
        Trans  Size : 16384(16K)
        Timeout : 5000 ms
     */
    public boolean Ft232_SetParameter(int nBufNum, int nBufSize, int nTranSize, int nReadTimeout)
    {
        if( m_d2xxDrvParameter == null ) return false;

        // 2 ~ 16
        if( nBufNum < 2 )       nBufNum = 2;
        else if( nBufNum > 16 ) nBufNum = 16;

        // 64 ~ 16384
        if( nBufSize < 64 )           nBufSize = 64;
        else if( nBufSize > 16384 )   nBufSize = 16384;

        // 64 ~ 16384
        if( nTranSize < 64 )           nTranSize = 64;
        else if( nTranSize > 16384 )   nTranSize = 16384;

        m_d2xxDrvParameter.setBufferNumber(nBufNum);
        m_d2xxDrvParameter.setMaxBufferSize(nBufSize);
        m_d2xxDrvParameter.setMaxTransferSize(nTranSize);
        m_d2xxDrvParameter.setReadTimeout(nReadTimeout);
        return true;
    }

    public boolean Ft232_SetMpsse()
    {
        boolean ok = true;
        ok &= m_ftDevice.setBitMode((byte)0x00, D2xxManager.FT_BITMODE_RESET);
        uSleep(1000);

        ok &= m_ftDevice.setBitMode((byte)0x00, D2xxManager.FT_BITMODE_MPSSE);
        uSleep(1000);
        return ok;
    }

    public boolean Ft232_SetLatencyTimer()
    {
        boolean ok =  m_ftDevice.setLatencyTimer( (byte)0x1 );
        uSleep(1000);
        return ok;
    }

    public boolean Ft232_SetCharacter()
    {
        boolean ok =  m_ftDevice.setChars( (byte) 0, (byte) 0, (byte) 0, (byte) 0 );
        uSleep(1000);
        return ok;
    }

    public boolean Ft232_PurgeDevice()
    {
        boolean ok =  m_ftDevice.purge( (byte)(D2xxManager.FT_PURGE_RX | D2xxManager.FT_PURGE_TX) );
        uSleep(1000);
        return ok;
    }
    public boolean Ft232_SetFlowControl()
    {
        boolean ok =  m_ftDevice.setFlowControl( D2xxManager.FT_FLOW_RTS_CTS, (byte)0x0, (byte)0x0 );
        uSleep(1000);
        return ok;
    }

    public boolean Ft232_SetClock(int nClockRate)
    {
        int value;
        byte[] wBuffer = new byte[4];
        Arrays.fill(wBuffer, (byte)0x0);
        if( nClockRate < DEF_FT232H.MID_6MHZ )
        {
            wBuffer[0] = (byte) DEF_FT232H.CMD_ENABLE_CLOCK_DIVIDE;
            m_ftDevice.write( wBuffer, 1);
            value = (DEF_FT232H.MID_6MHZ/nClockRate)-1;
        }
        else
        {
            wBuffer[0] = (byte) DEF_FT232H.CMD_DISABLE_CLOCK_DIVIDE;
            m_ftDevice.write( wBuffer, 1);
            value = (DEF_FT232H.MID_30MHZ/nClockRate)-1;
        }
        uSleep( 100 );

        wBuffer[0] = (byte) DEF_FT232H.CMD_MID_SET_CLOCK_FREQUENCY;
        wBuffer[1] = (byte)((value>>0)&0xFF);
        wBuffer[2] = (byte)((value>>8)&0xFF);
        int nSend = m_ftDevice.write( wBuffer, 3);
        uSleep( 100 );
        if( nSend == 3 ) return true;
        else             return false;
    }

    public void Ft232_Enable3PhaseClocking( boolean bEnable )
    {
        byte[] wBuffer = new byte[2];
        Arrays.fill(wBuffer, (byte)0x0);

        if( bEnable )
        {
            wBuffer[0] = (byte) DEF_FT232H.CMD_ENABLE_3PHASE_CLOCKING;
            m_ftDevice.write( wBuffer, 1);
        }
        else
        {
            wBuffer[0] = (byte) DEF_FT232H.CMD_DISABLE_3PHASE_CLOCKING;
            m_ftDevice.write( wBuffer, 1);
        }
        uSleep( 100 );
    }

    public void Ft232_EmptyDeviceInputBuffer()
    {
        byte[] dataInput = new byte[4096];
        int nByteInputBuffer = m_ftDevice.getQueueStatus();
        if( nByteInputBuffer > 0 )
        {
            do {
                if( nByteInputBuffer > 4096 )
                {
                    m_ftDevice.read( dataInput, 4096 );
                    nByteInputBuffer -= 4096;
                }
                else
                {
                    m_ftDevice.read( dataInput, nByteInputBuffer );
                    nByteInputBuffer -= nByteInputBuffer;
                }
            }while(nByteInputBuffer!=0);
        }
    }

    public void Ft232h_EnableDriveOnlyZero()
    {
        if( m_ftDevice == null ) return;

        byte[] wBuffer = new byte[4];
        Arrays.fill(wBuffer, (byte)0x0);

        Arrays.fill(wBuffer, (byte)0x0);
        wBuffer[0] = (byte) DEF_FT232H.CMD_MPSSE_ENABLE_DRIVE_ONLY_ZERO;
        wBuffer[1] = 0x3;  //Low  Byte
        wBuffer[2] = 0x0;  //High Byte
        m_ftDevice.write( wBuffer, 3);
        uSleep(50);
    }

    public void Ft232h_Init()
    {
        boolean b3PhaseClock = false;
        int nClockRate = DEF_FT232H.CLOCK_400KHZ;   //Fix.

        Ft232_ResetDevice();
        Ft232_PurgeDevice();
        Ft232_SetCharacter();
        Ft232_SetLatencyTimer();
        //Ft232_SetFlowControl();
        Ft232_SetMpsse();

        if(  b3PhaseClock ) nClockRate = ( nClockRate * 3 ) / 2;

        Ft232_SetClock( nClockRate );
        Ft232_SetGpioLow( (byte) DEF_FT232H.CMD_SET_LOW_BYTE_DATA_BITS_DATA, (byte) DEF_FT232H.CMD_SET_LOW_BYTE_DATA_BITS_DATA);
        Ft232_EmptyDeviceInputBuffer();
        Ft232_Enable3PhaseClocking( b3PhaseClock );
        mSleep(500);
    }

    public boolean Ft232_SetGpioLow(byte dir, byte val )
    {
        if( m_ftDevice == null ) return false;
        byte[] wBuffer = new byte[4];
        Arrays.fill(wBuffer, (byte)0x0);

        wBuffer[0] = (byte) DEF_FT232H.CMD_SET_LOW_BYTE_DATA_BITS;
        wBuffer[1] = val;
        wBuffer[2] = dir;
        int nSend = m_ftDevice.write( wBuffer, 3);
        uSleep(100);
        if( nSend == 3 ) return true;
        else             return false;
    }

    public boolean Ft232_GpioWrite( byte dir, byte val )
    {
        if( m_ftDevice == null ) return false;
        byte[] wBuffer = new byte[4];
        Arrays.fill(wBuffer, (byte)0x0);

        wBuffer[0] = (byte) DEF_FT232H.CMD_SET_HIGH_BYTE_DATA_BITS;
        wBuffer[1] = val;
        wBuffer[2] = dir;
        int nSend = m_ftDevice.write( wBuffer, 3);
        uSleep(100);
        if( nSend == 3 ) return true;
        else             return false;
    }

    public byte Ft232_GpioRead()
    {
        if( m_ftDevice == null ) return 0;
        byte[] wBuffer = new byte[3];
        Arrays.fill(wBuffer, (byte)0x0);

        wBuffer[0] = (byte) DEF_FT232H.CMD_GET_HIGH_BYTE_DATA_BITS;
        wBuffer[1] = (byte) DEF_FT232H.CMD_MPSSE_SEND_IMMEDIATE;
        m_ftDevice.write( wBuffer, 2);
        uSleep(100);

        byte[] rData = new byte[1];
        int nRx = m_ftDevice.read( rData, 1 );
        if( nRx != 1 ) return -1;
        return rData[0];
    }

    public boolean Ft232_LowByteWrite( byte dir, byte val )
    {
        if( m_ftDevice == null ) return false;

        byte[] wBuffer = new byte[4];
        Arrays.fill(wBuffer, (byte)0x0);

        wBuffer[0] = (byte) DEF_FT232H.CMD_SET_LOW_BYTE_DATA_BITS;
        wBuffer[1] = val;
        wBuffer[2] = dir;
        int nSend = m_ftDevice.write( wBuffer, 3);
        uSleep(100);
        if( nSend == 3 ) return true;
        else             return false;
    }

}
