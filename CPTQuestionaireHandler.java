package Handler.Order;

import BO.Order.CPTQuestionaire;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import utilities.Constants;
import utilities.Database;

public class CPTQuestionaireHandler {

    public List<CPTQuestionaire> selectQuestionaireMaster(String con, String odi,
            String orderStatusId) {

        String columns[] = {"-", "PATIENT_ID", "PATIENT_NAME", "AGE", "GENDER",
            "PATIENT_QUESTIONAIRE_ID", "ORDER_STATUS_ID", "QUESTIONAIRE_TYPE_ID",
            "QUESTIONAIRE_TYPE"};

        String query
                = " SELECT   PAT.PATIENT_ID   PATIENT_ID,                \n"
                + " PAT.FULL_NAME             PATIENT_NAME,            \n"
                + " ROUND(MONTHS_BETWEEN(SYSDATE,PAT.DOB )/12) AGE,     \n"
                + " GEN.DESCRIPTION           GENDER,                   \n"
                + " PQ.ID                     PATIENT_QUESTIONAIRE_ID,  \n"
                + " PQ.ORDER_STATUS_ID        ORDER_STATUS_ID,          \n"
                + " PQ.QUESTIONAIRE_TYPE_ID   QUESTIONAIRE_TYPE_ID,     \n"
                + " TYP.DESCRIPTION           QUESTIONAIRE_TYPE         \n"
                + " FROM                                                \n"
                + Database.DCMS.patientQuestionaire + " PQ,             \n"
                + Database.DCMS.patient + " PAT,                        \n"
                + Database.DCMS.definitionTypeDetail + " GEN ,          \n"
                + Database.DCMS.definitionType + " TYP                  \n"
                + " WHERE PQ.COMPLETE_ORDER_NO  = '" + con + "'           \n"
                + " AND PQ.ORDER_DETAIL_ID = '" + odi + "'              \n"
                + " AND PQ.ORDER_STATUS_ID = '" + orderStatusId + "'    \n"
                + " AND PQ.QUESTIONAIRE_TYPE_ID = TYP.ID                \n"
                + " AND PAT.GENDER_ID = GEN.ID                          \n"
                + " ORDER BY PQ.QUESTIONAIRE_TYPE_ID ASC                \n";

        return setDonorHistory(Constants.dao.selectDatainList(query, columns), con, odi);
    }

    public List<CPTQuestionaire> setDonorHistory(List<HashMap> list, String con, String odi) {

        List<CPTQuestionaire> vecList = new ArrayList<>();
        for (HashMap map : list) {
            CPTQuestionaire question = new CPTQuestionaire();
            question.setPatientId(map.get("PATIENT_ID").toString());
            question.setPatientName(map.get("PATIENT_NAME").toString());
            question.setPatientAge(map.get("AGE").toString());
            question.setPatientGender(map.get("GENDER").toString());
            question.setPatientQuestionaireId(map.get("PATIENT_QUESTIONAIRE_ID").toString());
            question.setCompleteOrderNo(map.get("COMPLETE_ORDER_NO").toString());
            question.setOrderDetailId(map.get("ORDER_DETAIL_ID").toString());
            question.setOrderStatusId(map.get("ORDER_STATUS_ID").toString());
            question.setQuestionaireTypeId(map.get("QUESTIONAIRE_TYPE_ID").toString());
            question.setQuestionaireType(map.get("QUESTIONAIRE_TYPE").toString());
            vecList.add(question);
        }
        return vecList;
    }

    public List<CPTQuestionaire> selectQuestionaireDetail(String con, String odi,
            String orderStatusId, String questionTypeId) {

        String colums[] = {"-", "GENERAL_REMARKS",
            "PATIENT_QUESTIONAIRE_ID", "QUESTION_DETAIL_ID",
            "QUESTION_ID", "QUESTION_DESCRIPTION", "QUESTION_RESULT",
            "QUESTION_REMARKS"};

        String query = " SELECT NVL(PQ.GENERAL_REMARKS, ' ')GENERAL_REMARKS, \n"
                + " PQD.PATIENT_QUESTIONAIRE_ID  PATIENT_QUESTIONAIRE_ID,   \n"
                + " PQD.ID                       QUESTION_DETAIL_ID,        \n"
                + " PQD.QUESTION_ID              QUESTION_ID,               \n"
                + " DTD.DESCRIPTION              QUESTION_DESCRIPTION,      \n"
                + " PQD.RESULT                   QUESTION_RESULT,           \n"
                + " NVL(PQD.REMARKS, ' ')        QUESTION_REMARKS           \n"
                + " FROM                                                    \n"
                + Database.DCMS.patientQuestionaire + " PQ,                 \n"
                + Database.DCMS.patientQuestionaireDetail + " PQD,          \n"
                + Database.DCMS.definitionTypeDetail + " DTD                \n"
                + " WHERE PQ.COMPLETE_ORDER_NO = '" + con + "'              \n"
                + " AND PQ.ORDER_DETAIL_ID = " + odi + "                    \n"
                + " AND PQ.ORDER_STATUS_ID = " + orderStatusId + "          \n"
                + " AND PQ.QUESTIONAIRE_TYPE_ID = " + questionTypeId + "    \n"
                + " AND PQ.ID = PQD.PATIENT_QUESTIONAIRE_ID                 \n"
                + " AND PQD.QUESTION_ID = DTD.ID                            \n"
                + " ORDER BY DTD.ADDITIONAL_INFO ";

        List<HashMap> lis = Constants.dao.selectDatainList(query, colums);
        List<CPTQuestionaire> vecList = new ArrayList<>();
        for (HashMap map : lis) {
            CPTQuestionaire donorHistoryDetail = new CPTQuestionaire();
            donorHistoryDetail.setPatientQuestionaireId(map.get("PATIENT_QUESTIONAIRE_ID").toString());
            donorHistoryDetail.setQuestionDetailId(map.get("QUESTION_DETAIL_ID").toString());
            donorHistoryDetail.setQuestionId(map.get("QUESTION_ID").toString());
            donorHistoryDetail.setQuestionDescription(map.get("QUESTION_DESCRIPTION").toString());
            donorHistoryDetail.setQuestionResult(map.get("QUESTION_RESULT").toString());
            donorHistoryDetail.setQuestionRemarks(map.get("QUESTION_REMARKS").toString());
            donorHistoryDetail.setGeneralRemarks(map.get("GENERAL_REMARKS").toString());
            vecList.add(donorHistoryDetail);
        }
        return vecList;

    }

    public boolean saveCPTQuestionaireDetail(List<CPTQuestionaire> listDetail,
            CPTQuestionaire master) {

        List<String> listUpdates = new ArrayList<>();

        for (CPTQuestionaire question : listDetail) {

            String query = " UPDATE " + Database.DCMS.patientQuestionaireDetail + "  \n"
                    + " SET RESULT = '" + question.getQuestionResult() + "',              \n"
                    + " REMARKS = '" + question.getQuestionRemarks().trim().replaceAll("'", "''") + "'  \n"
                    + " WHERE PATIENT_QUESTIONAIRE_ID = '" + question.getPatientQuestionaireId() + "'               \n"
                    + " AND QUESTION_ID = '" + question.getQuestionId() + "' \n"
                    + " AND ID = '" + question.getQuestionDetailId() + "'    \n";
            listUpdates.add(query);
        }

        boolean ret = Constants.dao.executeUpdates(listUpdates);

        if (ret) {
            String query = " UPDATE " + Database.DCMS.patientQuestionaire + "     \n"
                    + " SET GENERAL_REMARKS = '" + master.getGeneralRemarks().trim() + "'    \n"
                    + " WHERE COMPLETE_ORDER_NO = '" + master.getCompleteOrderNo() + "'      \n"
                    + " AND ORDER_DETAIL_ID = " + master.getOrderDetailId() + "              \n"
                    + " AND QUESTIONAIRE_TYPE_ID = " + master.getQuestionaireTypeId() + "    \n";
            ret = Constants.dao.executeUpdate(query, false);
        }
        return ret;
    }
}
