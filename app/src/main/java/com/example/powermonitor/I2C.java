package com.example.powermonitor;

import android.content.Context;
import android.util.Log;

import java.util.Arrays;

public class I2C extends CFT232h
{
    private Context OpenDeviceFragmentContext;
    private int m_AdcConfigData;
    private int m_AdcSampling;

    public I2C(Context parentContext)
    {
        super(parentContext);
        OpenDeviceFragmentContext = parentContext;
        I2C_BuildAdcConfigData( (byte)0x0 );
        I2C_SetSampling( 5 );
    }

    public void I2C_SetSampling( int sampling)
    {
        m_AdcSampling = sampling;
    }

    public int I2C_GetSampling()
    {
        return m_AdcSampling;
    }

    public int I2C_GetAdcConfigValue()
    {
        return m_AdcConfigData;
    }

    public void I2C_Init()
    {
        this.Ft232h_Init();
    }

    public boolean I2C_Start()
    {
        int iSendByte = 0x0;
        byte[] wBuffer = new byte[256];
        Arrays.fill(wBuffer, (byte)0x0);

        /* SCL high, SDA high */
        for ( int j = 0; j< I2C.DEF_FT232H.START_DURATION_1; j++)
        {
            wBuffer[iSendByte++] = (byte)I2C.DEF_FT232H.CMD_SET_LOW_BYTE_DATA_BITS;    //0x80
            wBuffer[iSendByte++] = (byte)I2C.DEF_FT232H.VALUE_SCLHIGH_SDAHIGH;         //0x3
            wBuffer[iSendByte++] = (byte)I2C.DEF_FT232H.DIRECTION_SCLOUT_SDAIN;        //0x11  Make this input instead to let line be pulled up
        }
        /* SCL high, SDA low */
        for ( int j = 0; j<I2C.DEF_FT232H.START_DURATION_2; j++)
        {
            wBuffer[iSendByte++] = (byte)I2C.DEF_FT232H.CMD_SET_LOW_BYTE_DATA_BITS;  //0x80
            wBuffer[iSendByte++] = (byte)I2C.DEF_FT232H.VALUE_SCLHIGH_SDALOW;        //0x1
            wBuffer[iSendByte++] = (byte)I2C.DEF_FT232H.DIRECTION_SCLOUT_SDAOUT;     //0x13
        }
        /*SCL low, SDA low */
        wBuffer[iSendByte++] = (byte)I2C.DEF_FT232H.CMD_SET_LOW_BYTE_DATA_BITS;
        wBuffer[iSendByte++] = (byte)I2C.DEF_FT232H.VALUE_SCLLOW_SDALOW;
        wBuffer[iSendByte++] = (byte)I2C.DEF_FT232H.DIRECTION_SCLOUT_SDAOUT;

        int nSend = m_ftDevice.write( wBuffer, iSendByte);
        if( iSendByte != nSend ) return false;
        return true;
    }

    public boolean I2C_Stop()
    {
        int iSendByte = 0x0;
        byte[] wBuffer = new byte[256];
        Arrays.fill(wBuffer, (byte)0x0);

        /* SCL low, SDA low */
        for ( int j = 0; j<I2C.DEF_FT232H.STOP_DURATION_1; j++ )
        {
            wBuffer[iSendByte++] = (byte)I2C.DEF_FT232H.CMD_SET_LOW_BYTE_DATA_BITS;  //0x80
            wBuffer[iSendByte++] = (byte)I2C.DEF_FT232H.VALUE_SCLLOW_SDALOW;         //0x00
            wBuffer[iSendByte++] = (byte)I2C.DEF_FT232H.DIRECTION_SCLOUT_SDAOUT;     //0x13
        }

        /* SCL high, SDA low */
        for ( int j = 0; j<I2C.DEF_FT232H.STOP_DURATION_2; j++ )
        {
            wBuffer[iSendByte++] = (byte)I2C.DEF_FT232H.CMD_SET_LOW_BYTE_DATA_BITS;  //0x80
            wBuffer[iSendByte++] = (byte)I2C.DEF_FT232H.VALUE_SCLHIGH_SDALOW;        //0x1
            wBuffer[iSendByte++] = (byte)I2C.DEF_FT232H.DIRECTION_SCLOUT_SDAOUT;     //0x13
        }

        /* SCL high, SDA high */
        for ( int j = 0; j<I2C.DEF_FT232H.STOP_DURATION_3; j++ )
        {
            wBuffer[iSendByte++] = (byte)I2C.DEF_FT232H.CMD_SET_LOW_BYTE_DATA_BITS;  //x80
            wBuffer[iSendByte++] = (byte)I2C.DEF_FT232H.VALUE_SCLHIGH_SDAHIGH;       //0x3
            wBuffer[iSendByte++] = (byte)I2C.DEF_FT232H.DIRECTION_SCLOUT_SDAIN;      //0x11 // Make this input instead to let line be pulled up
        }
        wBuffer[iSendByte++] = (byte)I2C.DEF_FT232H.CMD_SET_LOW_BYTE_DATA_BITS;
        wBuffer[iSendByte++] = (byte)I2C.DEF_FT232H.VALUE_SCLHIGH_SDAHIGH;
        wBuffer[iSendByte++] = (byte)I2C.DEF_FT232H.DIRECTION_SCLIN_SDAIN; /* Tristate the SCL & SDA pins */

        int nSend = m_ftDevice.write( wBuffer, iSendByte);
        if( iSendByte != nSend ) return false;
        return true;
    }

    public boolean I2C_Write8bitAndGetAck( byte buffer )
    {
        boolean bAck = false;
        byte[] rBuffer = new byte[ 6];
        byte[] wBuffer = new byte[64];
        int nBytesTx   = 0;
        int nBytesTxd  = 0;

        Arrays.fill(wBuffer, (byte)0x0);

        /* Set direction */
        wBuffer[nBytesTx++] = (byte)I2C.DEF_FT232H.CMD_SET_LOW_BYTE_DATA_BITS;   //0x80
        wBuffer[nBytesTx++] = (byte)I2C.DEF_FT232H.VALUE_SCLLOW_SDALOW;          //0x00
        wBuffer[nBytesTx++] = (byte)I2C.DEF_FT232H.DIRECTION_SCLOUT_SDAOUT;      //0x13

        /* Command to write 8 bits */
        wBuffer[nBytesTx++] = (byte)I2C.DEF_FT232H.CMD_MPSSE_DATA_OUT_BITS_NEG_EDGE; //0x13
        wBuffer[nBytesTx++] = (byte)I2C.DEF_FT232H.DATA_SIZE_8BITS;                 //0x07
        wBuffer[nBytesTx++] = buffer;                                           //data

        /* Set SDA to input mode before reading ACK bit */
        wBuffer[nBytesTx++] = (byte)I2C.DEF_FT232H.CMD_SET_LOW_BYTE_DATA_BITS;   //0x80
        wBuffer[nBytesTx++] = (byte)I2C.DEF_FT232H.VALUE_SCLLOW_SDALOW;          //0x00
        wBuffer[nBytesTx++] = (byte)I2C.DEF_FT232H.DIRECTION_SCLOUT_SDAIN;       //0x11

        /* Command to get ACK bit */
        wBuffer[nBytesTx++] = (byte)I2C.DEF_FT232H.CMD_MPSSE_DATA_IN_BITS_POS_EDGE;  //0x22 -- READ
        wBuffer[nBytesTx++] = (byte)I2C.DEF_FT232H.DATA_SIZE_1BIT;                   //0x00

        /* Command MPSSE to send data to PC immediately */
        wBuffer[nBytesTx++] = (byte)I2C.DEF_FT232H.CMD_MPSSE_SEND_IMMEDIATE;         //0x87

        nBytesTxd = m_ftDevice.write( wBuffer, nBytesTx);
        if( nBytesTx != nBytesTxd )
        {
            Log.e("SlaveAddress","Device Address Write Error!!");
            return false;
        }

        nBytesTx = 1;
        nBytesTxd = m_ftDevice.read( rBuffer, nBytesTx);
        if( nBytesTx != nBytesTxd )
        {
            Log.e("SlaveAddress","Ack Read Error!");
            return false;
        }
        if( (rBuffer[0]&0x1) != 0 ) bAck = true;
        else                        bAck = false;
        return bAck;
    }

    public boolean I2C_WriteDeviceAddress( int SlaveAddress, boolean direction )
    {
        boolean bAck = false;
        byte deviceAddress = (byte)((SlaveAddress&0xFF)<<1);
        byte wMask = (byte)I2C.DEF_FT232H.I2C_ADDRESS_WRITE_MASK;
        byte rMask = (byte)I2C.DEF_FT232H.I2C_ADDRESS_READ_MASK;

        if( direction ) deviceAddress = (byte)(deviceAddress | rMask );
        else            deviceAddress = (byte)(deviceAddress & wMask );

        bAck = I2C_Write8bitAndGetAck( deviceAddress );
        return bAck;
    }

    public FT_STATUS I2C_DeviceWrite( int SlaveAddress, int i2cNumBytesToTx, byte[] buffer, int option )
    {
        boolean bAddressAck = false;
        int nTrans = 0;

        Ft232_PurgeDevice();
        if( (option & I2C.DEF_FT232H.I2C_TRANSFER_OPTIONS_START_BIT) != 0 )
        {
            if( !I2C_Start() )
                return FT_STATUS.FT_FAIL_TO_WRITE_DEVICE;
        }

        bAddressAck = I2C_WriteDeviceAddress( SlaveAddress, false );   //Write
        if( !bAddressAck )      /*ack bit set actually means device nAcked*/
        {
            boolean bDataAck = false;
            for( nTrans=0;nTrans<i2cNumBytesToTx;nTrans++ )
            {
                bDataAck = I2C_Write8bitAndGetAck( buffer[nTrans] );
                if( bDataAck )
                {
                    Log.e("I2C_Write","Data send ack error!");
                    return FT_STATUS.FT_FAIL_TO_WRITE_DEVICE;
                }
            }
            if( i2cNumBytesToTx != nTrans )
            {
                Log.e("I2C_Write","Data send size mismatch!");
                return FT_STATUS.FT_IO_ERROR;
            }
            if( (option & I2C.DEF_FT232H.I2C_TRANSFER_OPTIONS_STOP_BIT) != 0 )
            {
                if( !I2C_Stop() )
                    return FT_STATUS.FT_FAIL_TO_WRITE_DEVICE;
            }
        }
        else
        {
            if( (option & I2C.DEF_FT232H.I2C_TRANSFER_OPTIONS_STOP_BIT) != 0 )
                I2C_Stop();
            return FT_STATUS.FT_DEVICE_NOT_FOUND;
        }
        return FT_STATUS.FT_OK;
    }

    public byte I2C_Read8bitsAndGiveAck( boolean bUseAck )
    {
        byte[] rBuffer = new byte[ 4];
        byte[] wBuffer = new byte[64];
        byte rByte = 0;
        int nBytesTx  = 0;
        int nBytesTxd = 0;

        Arrays.fill(wBuffer, (byte)0x0);
        /* Set pin directions - SCL is output driven low, SDA is input (set high but does not matter) */
        wBuffer[nBytesTx++] = (byte)I2C.DEF_FT232H.CMD_SET_LOW_BYTE_DATA_BITS;   //0x80
        wBuffer[nBytesTx++] = (byte)I2C.DEF_FT232H.VALUE_SCLLOW_SDALOW;          //0x00
        wBuffer[nBytesTx++] = (byte)I2C.DEF_FT232H.DIRECTION_SCLOUT_SDAIN;       //0x11

        /* Command to read 8 bits */
        wBuffer[nBytesTx++] = (byte)I2C.DEF_FT232H.CMD_MPSSE_DATA_IN_BITS_POS_EDGE; //0x22 -- READ
        wBuffer[nBytesTx++] = (byte)I2C.DEF_FT232H.DATA_SIZE_8BITS;                 //0x07

        if( bUseAck )
        {
            /* We will drive the ACK bit to a '0' so pre-set pin to a '0' */
            wBuffer[nBytesTx++] = (byte)I2C.DEF_FT232H.CMD_SET_LOW_BYTE_DATA_BITS;  //0x80
            wBuffer[nBytesTx++] = (byte)I2C.DEF_FT232H.VALUE_SCLLOW_SDALOW;         //0x00
            wBuffer[nBytesTx++] = (byte)I2C.DEF_FT232H.DIRECTION_SCLOUT_SDAOUT;     //0x13

            /* Clock out the ack bit as a '0' on negative edge */
            wBuffer[nBytesTx++] = (byte)I2C.DEF_FT232H.CMD_MPSSE_DATA_OUT_BITS_NEG_EDGE;    //0x13
            wBuffer[nBytesTx++] = (byte)I2C.DEF_FT232H.DATA_SIZE_1BIT;                      //0x00
            wBuffer[nBytesTx++] = (byte)I2C.DEF_FT232H.SEND_ACK;                            //0x00
        }
        else
        {
            /* We will release the ACK bit to a '1' so pre-set pin to a '1' by making it an input */
            wBuffer[nBytesTx++] = (byte)I2C.DEF_FT232H.CMD_SET_LOW_BYTE_DATA_BITS;  //0x80
            wBuffer[nBytesTx++] = (byte)I2C.DEF_FT232H.VALUE_SCLLOW_SDALOW;         //0x00
            wBuffer[nBytesTx++] = (byte)I2C.DEF_FT232H.DIRECTION_SCLOUT_SDAIN;      //0x11

            /* Clock out the ack bit as a '1' on negative edge - never actually seen on line since SDA is input but burns off one bit time */
            wBuffer[nBytesTx++] = (byte)I2C.DEF_FT232H.CMD_MPSSE_DATA_OUT_BITS_NEG_EDGE;    //0x13
            wBuffer[nBytesTx++] = (byte)I2C.DEF_FT232H.DATA_SIZE_1BIT;                      //0x00
            wBuffer[nBytesTx++] = (byte)I2C.DEF_FT232H.SEND_NACK;                           //0x80
        }
        /* Back to Idle */
        wBuffer[nBytesTx++] = (byte)I2C.DEF_FT232H.CMD_SET_LOW_BYTE_DATA_BITS;      //0x80
        wBuffer[nBytesTx++] = (byte)I2C.DEF_FT232H.VALUE_SCLLOW_SDALOW;             //0x00
        wBuffer[nBytesTx++] = (byte)I2C.DEF_FT232H.DIRECTION_SCLOUT_SDAIN;          //0x11

        /* Command MPSSE to send data to PC immediately */
        wBuffer[nBytesTx++] = (byte)I2C.DEF_FT232H.CMD_MPSSE_SEND_IMMEDIATE;        //0x87

        nBytesTxd = m_ftDevice.write( wBuffer, nBytesTx);
        if( nBytesTx != nBytesTxd )
        {
            Log.e("I2C READ","Read command write error");
            return -1;
        }
        uSleep(100);

        nBytesTx = 1;
        nBytesTxd = m_ftDevice.read( rBuffer, nBytesTx);
        if( nBytesTx != nBytesTxd )
        {
            Log.e("I2C READ","Read data error!");
            return -1;
        }
        rByte = rBuffer[0];
        return rByte;
    }

    public FT_STATUS I2C_DeviceRead( int SlaveAddress, int i2cNumBytesToRx, byte[] buffer, int option )
    {
        boolean bAddressAck = false;
        int nTrans = 0;
        Ft232_PurgeDevice();
        if( (option & I2C.DEF_FT232H.I2C_TRANSFER_OPTIONS_START_BIT) != 0 )
        {
            if( !I2C_Start() )
                return FT_STATUS.FT_FAIL_TO_WRITE_DEVICE;
        }

        bAddressAck = I2C_WriteDeviceAddress( SlaveAddress, true );
        if( !bAddressAck )
        {
            for( nTrans=0;nTrans<i2cNumBytesToRx;nTrans++ )
            {
                buffer[nTrans] = I2C_Read8bitsAndGiveAck( true );
            }
            if( i2cNumBytesToRx != nTrans )
            {
                Log.e("I2C_Write","Data send size mismatch!");
                return FT_STATUS.FT_IO_ERROR;
            }
            if( (option & I2C.DEF_FT232H.I2C_TRANSFER_OPTIONS_STOP_BIT) != 0 )
            {
                if( !I2C_Stop() )
                    return FT_STATUS.FT_FAIL_TO_WRITE_DEVICE;
            }
        }
        else
        {
            if( (option & I2C.DEF_FT232H.I2C_TRANSFER_OPTIONS_STOP_BIT) != 0 )
                I2C_Stop();
            return FT_STATUS.FT_DEVICE_NOT_FOUND;
        }
        return FT_STATUS.FT_OK;
    }

    public int I2C_GetAdcConfigRegister()
    {
        byte[] wBuffer = new byte[4];
        byte[] rBuffer = new byte[4];
        int nTx    = 0;
        final int nRx    = 2;
        int nSlaveAddress = I2C.DEF_FT232H.ADC_SLAVE_ADDRESS;
        int option  = I2C.DEF_FT232H.I2C_TRANSFER_OPTIONS_START_BIT | I2C.DEF_FT232H.I2C_TRANSFER_OPTIONS_STOP_BIT;

        Arrays.fill( wBuffer, (byte)0x0 );
        Arrays.fill( rBuffer, (byte)0x0 );

        wBuffer[nTx++] = (byte)I2C.DEF_FT232H.ADC_CONFIGURATION_REG;
        I2C_DeviceWrite( nSlaveAddress, nTx, wBuffer, option );
        uSleep(500);
        I2C_DeviceRead ( nSlaveAddress, nRx, rBuffer, option );
        int msb = rBuffer[0]&0xFF;
        int lsb = rBuffer[1]&0xFF;
        return(msb<<8 | lsb);
    }

    public int I2C_SetAdcConfig()
    {
        int nTx;
        int nRx = 2;
        int option  = I2C.DEF_FT232H.I2C_TRANSFER_OPTIONS_START_BIT | I2C.DEF_FT232H.I2C_TRANSFER_OPTIONS_STOP_BIT;
        int slaveAddress = I2C.DEF_FT232H.ADC_SLAVE_ADDRESS;
        byte[] wBuffer = new byte[4];
        byte[] rBuffer = new byte[4];

        Arrays.fill( wBuffer, (byte)0x0 );
        Arrays.fill( rBuffer, (byte)0x0 );
        I2C_BuildAdcConfigData( (byte)0x0 );

        nTx = 0;
        wBuffer[nTx++] = (byte)I2C.DEF_FT232H.ADC_CONFIGURATION_REG;
        wBuffer[nTx++] = (byte)((m_AdcConfigData>>8)&0xFF);
        wBuffer[nTx++] = (byte)((m_AdcConfigData>>0)&0xFF);
        I2C_DeviceWrite( slaveAddress, nTx, wBuffer, option );
        uSleep(500);

        nTx = 0;
        wBuffer[nTx++] = (byte)I2C.DEF_FT232H.ADC_CONFIGURATION_REG;
        this.I2C_DeviceWrite( slaveAddress, nTx, wBuffer, option );
        this.I2C_DeviceRead ( slaveAddress, nRx, rBuffer, option );
        int msb = rBuffer[0]&0xFF;
        int lsb = rBuffer[1]&0xFF;
        //short sData = (short)(msb<<8 | lsb);
        return (msb<<8 | lsb);
    }

    public double I2C_AdcRead()
    {
        final double ADC_LSB = ((2.048*2.0)/4096.0);
        int nTx = 0;
        int nRx = 2;
        int option  = I2C.DEF_FT232H.I2C_TRANSFER_OPTIONS_START_BIT | I2C.DEF_FT232H.I2C_TRANSFER_OPTIONS_STOP_BIT;
        int slaveAddress = I2C.DEF_FT232H.ADC_SLAVE_ADDRESS;
        byte[] wBuffer = new byte[4];
        byte[] rBuffer = new byte[4];

        Arrays.fill( wBuffer, (byte)0x0 );
        wBuffer[nTx++] = (byte) I2C.DEF_FT232H.ADC_CONVERSION_REG;
        I2C_DeviceWrite( slaveAddress, nTx, wBuffer, option );
        uSleep(100);

        double dMeasSum = 0.0;
        double dMeas = 0.0;
        for( int nCount=0;nCount<this.I2C_GetSampling();nCount++ )
        {
            this.I2C_DeviceRead ( slaveAddress, nRx, rBuffer, option );
            int msb = rBuffer[0]&0xFF;
            int lsb = rBuffer[1]&0xFF;
            short val = (short)((msb<<8 | lsb)&0xFFFF);
            val = (short)(val >> 4);
            dMeas = (double)(val) * ADC_LSB;
            dMeasSum += dMeas;
        }
        return dMeasSum / this.I2C_GetSampling();
    }

    /* ADC External Mux*/
    public FT_STATUS I2C_TMuxSet(byte mux)
    {
        if( mux < 0 || mux > 3 )
            return FT_STATUS.FT_FAIL_TO_WRITE_DEVICE;

        final byte dir = 0x3;
        Ft232_GpioWrite( dir, mux );
        return FT_STATUS.FT_OK;
    }

    /* ADC Internal Mux*/
    public FT_STATUS I2C_AMuxSet( byte mux )
    {
        if( mux < 1 || mux > 3 )
            return FT_STATUS.FT_FAIL_TO_WRITE_DEVICE;

        int nTx;
        int nRx = 2;
        int option  = I2C.DEF_FT232H.I2C_TRANSFER_OPTIONS_START_BIT | I2C.DEF_FT232H.I2C_TRANSFER_OPTIONS_STOP_BIT;
        int slaveAddress = I2C.DEF_FT232H.ADC_SLAVE_ADDRESS;
        byte[] wBuffer = new byte[4];
        byte[] rBuffer = new byte[4];
        Arrays.fill( wBuffer, (byte)0x0 );
        Arrays.fill( rBuffer, (byte)0x0 );
        I2C_BuildAdcConfigData( mux );

        nTx = 0;
        wBuffer[nTx++] = (byte)I2C.DEF_FT232H.ADC_CONFIGURATION_REG;
        wBuffer[nTx++] = (byte)((m_AdcConfigData>>8)&0xFF);
        wBuffer[nTx++] = (byte)((m_AdcConfigData>>0)&0xFF);
        I2C_DeviceWrite( slaveAddress, nTx, wBuffer, option );
        uSleep(200);

        nTx = 0;
        wBuffer[nTx++] = (byte)I2C.DEF_FT232H.ADC_CONFIGURATION_REG;
        this.I2C_DeviceWrite( slaveAddress, nTx, wBuffer, option );
        this.I2C_DeviceRead ( slaveAddress, nRx, rBuffer, option );
        int msb = rBuffer[0]&0xFF;
        int lsb = rBuffer[1]&0xFF;

        return FT_STATUS.FT_OK;
    }

    public void I2C_BuildAdcConfigData(byte mux)
    {
        int OS        = 0x1;
        int MUX       = mux;
        int PGA       = 0x2;	// 0x0:6.144, 0x1:4.096, 0x2:2.048(default), 0x3:1.024, 0x4:0.512 , ....
        int MODE      = 0x1;
        int DR        = 0x4;	//
        int RESERVED  = 0x3;	//Always write 03h

        m_AdcConfigData = 0x0;
        m_AdcConfigData |= (OS      &0x01)<<15;
        m_AdcConfigData |= (MUX     &0x07)<<12;
        m_AdcConfigData |= (PGA     &0x07)<< 9;
        m_AdcConfigData |= (MODE    &0x01)<< 8;
        m_AdcConfigData |= (DR      &0x07)<< 5;
        m_AdcConfigData |= (RESERVED&0x1F)<< 0;
    }
}
