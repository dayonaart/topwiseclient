package id.co.payment2go.terminalsdkhelper.verifone.emv.tlv.caseB;

import id.co.payment2go.terminalsdkhelper.verifone.emv.tlv.ISO8583;

/**
 * Created by Simon on 2018/8/27.
 */

public class ISO8583CaseB extends ISO8583 {

    static int [][] FIELD_ATTRIBUTE_ARRAY =
            {
                    {	TYPE_BCD	,	4, 0, 0	},	// field	0
                    {	TYPE_BIN	,	8, 0, 0	},	// field	1
                    {	TYPE_L_BCD	,	0, 0, 0	},	// field	2
                    {	TYPE_BCD	,	6, 0, 0	},	// field	3
                    {	TYPE_BCD	,	12, 0, 0	},	// field	4
                    {	TYPE_BCD	,	12, 0, 0	},	// field	5
                    {	TYPE_BCD	,	12, 0, 0	},	// field	6
                    {	TYPE_BCD	,	10, 0, 0	},	// field	7
                    {	TYPE_ASC	,	1, 0, 0	},	// field	8
                    {	TYPE_BCD	,	8, 0, 0	},	// field	9
                    {	TYPE_BCD	,	8, 0, 0	},	// field	10
                    {	TYPE_BCD	,	6, 0, 0	},	// field	11
                    {	TYPE_BCD	,	6, 0, 0	},	// field	12
                    {	TYPE_BCD	,	8, 0, 0	},	// field	13
                    {	TYPE_BCD	,	4, 0, 0	},	// field	14
                    {	TYPE_BCD	,	8, 0, 0	},	// field	15
                    {	TYPE_ASC	,	1, 0, 0	},	// field	16
                    {	TYPE_BCD	,	4, 0, 0	},	// field	17
                    {	TYPE_BCD	,	5, 0, 0	},	// field	18
                    {	TYPE_BCD	,	3, 0, 0	},	// field	19
                    {	TYPE_BCD	,	3, 0, 0	},	// field	20
                    {	TYPE_ASC	,	7, 0, 0	},	// field	21
                    {	TYPE_BCD	,	4, 0, 0	},	// field	22
                    {	TYPE_BCD	,	4, 0, 0	},	// field	23
                    {	TYPE_ASC	,	2, 0, 0	},	// field	24
                    {	TYPE_ASC	,	2, 0, 0	},	// field	25
                    {	TYPE_BCD	,	2, 0, 0	},	// field	26
                    {	TYPE_BCD	,	2, 0, 0	},	// field	27
                    {	TYPE_BCD	,	2, 0, 0	},	// field	28
                    {	TYPE_ASC	,	8, 0, 0	},	// field	29
                    {	TYPE_BCD	,	8, 0, 0	},	// field	30
                    {	TYPE_BCD	,	8, 0, 0	},	// field	31
                    {	TYPE_L_BCD	,	11, 0, 0	},	// field	32
                    {	TYPE_L_BCD	,	11, 0, 0	},	// field	33
                    {	TYPE_L_BCD	,	28, 0, 0	},	// field	34
                    {	TYPE_LL_ASC	,	0, 0, 0	},	// field	35
                    {	TYPE_LL_BCD	,	104, 0, 0	},	// field	36
                    {	TYPE_ASC	,	23, 0, 0	},	// field	37
                    {	TYPE_ASC	,	6, 0, 0	},	// field	38
                    {	TYPE_ASC	,	2, 0, 0	},	// field	39
                    {	TYPE_ASC	,	3, 0, 0	},	// field	40
                    {	TYPE_ASC	,	15, 0, 0	},	// field	41
                    {	TYPE_ASC	,	12, 0, 0	},	// field	42
                    {	TYPE_ASC	,	40, 0, 0	},	// field	43
                    {	TYPE_L_ASC	,	99, 0, 0	},	// field	44
                    {	TYPE_L_ASC	,	76, 0, 0	},	// field	45
                    {	TYPE_LL_ASC	,	999, 0, 0	},	// field	46
                    {	TYPE_LL_ASC	,	999, 0, 0	},	// field	47
                    {	TYPE_L_BCD	,	19, 0, 0	},	// field	48
                    {	TYPE_ASC	,	3, 0, 0	},	// field	49
                    {	TYPE_ASC	,	3, 0, 0	},	// field	50
                    {	TYPE_ASC	,	3, 0, 0	},	// field	51
                    {	TYPE_BIN	,	8, 0, 0	},	// field	52
                    {	TYPE_BCD	,	18, 0, 0	},	// field	53
                    {	TYPE_L_BCD	,	120, 0, 0	},	// field	54
                    {	TYPE_LL_BIN	,	999, 0, 0	},	// field	55
                    {	TYPE_LL_ASC	,	999, 0, 0	},	// field	56
                    {	TYPE_LL_ASC	,	999, 0, 0	},	// field	57
                    {	TYPE_LL_ASC	,	999, 0, 0	},	// field	58
                    {	TYPE_LL_ASC	,	999, 0, 0	},	// field	59
                    {	TYPE_BIN	,	8, 0, 0	},	// field	60
                    {	TYPE_BIN	,	8, 0, 0	},	// field	61
                    {	TYPE_ASC	,	75, 0, 0	},	// field	62
                    {	TYPE_ASC	,	12, 0, 0	},	// field	63
                    {	TYPE_BIN	,	8, 0, 0	},	// field	64
            };
    public ISO8583CaseB(){
        super.attribute_array = this.FIELD_ATTRIBUTE_ARRAY;
    }

}
