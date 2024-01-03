package id.co.payment2go.terminalsdkhelper.ingenico.emv.util;

/**
 * EMV parameter.
 */

interface EmvParameter {

    String pack() throws EmvParameterException;
}
