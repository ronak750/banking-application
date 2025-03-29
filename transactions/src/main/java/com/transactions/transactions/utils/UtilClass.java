package com.transactions.transactions.utils;

import com.transactions.transactions.dto.commons.StatusModel;
import com.transactions.transactions.dto.response.*;
import com.transactions.transactions.dto.request.WalletTransactionResponseDTO;
import com.transactions.transactions.entity.Transaction;
import com.transactions.transactions.exception.ApiException;
import com.transactions.transactions.exception.InvalidFieldException;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static com.transactions.transactions.constant.Constants.INTERNAL_SERVER_ERROR;
import static com.transactions.transactions.constant.Constants.SOMETHING_WENT_WRONG_MSG;

public class UtilClass {

    private UtilClass() {}

    public static WalletTransactionResponseDTO convertTransactionToWalletTransactionResponseDto(Transaction transaction) {
        return new WalletTransactionResponseDTO(
                   transaction.getTransactionId(),
                    transaction.getAmount(),
                    transaction.getSourceId(),
                    transaction.getDestinationId(),
                    transaction.getTransactionStatus(),
                    transaction.getDescription(),
                    transaction.getCreatedAt() == null ? LocalDateTime.now() : transaction.getCreatedAt()
                );
    }

    public static TransactionResponseDTO convertTransactionToTransactionResponseDto(Transaction transaction) {
        return new TransactionResponseDTO(
                transaction.getTransactionId(),
                transaction.getAmount(),
                transaction.getSourceId(),
                transaction.getDestinationId(),
                transaction.getTransactionStatus(),
                transaction.getDescription(),
                transaction.getCreatedAt() == null ? LocalDateTime.now() : transaction.getCreatedAt()
        );
    }

    public static BigDecimal fetchBalanceFromGatewayConnectorBalanceResponseDTO(GatewayConnectorBalanceResponseDTO responseDTO) {
        validateGatewayConnectorResponses(
                responseDTO.getResponse(),
                responseDTO.getStatusModel(),
                responseDTO.getResponseMsg() == null ? SOMETHING_WENT_WRONG_MSG : responseDTO.getResponseMsg()
        );
        return responseDTO.getResponse().getBalance();
    }


    public static TransactionResponseDTO fetchTransactionFromGatewayConnectorResponseDTO(GatewayConnectorTransactionResponseDTO responseDTO) {
        validateGatewayConnectorResponses(
                responseDTO.getResponse(),
                responseDTO.getStatusModel(),
                responseDTO.getResponseMsg() == null ? SOMETHING_WENT_WRONG_MSG : responseDTO.getResponseMsg()
        );
        return responseDTO.getResponse();
    }

    public static List<TransactionResponseDTO> getListOfTransactionsFromGatewayConnectorResponse(GatewayConnectorTransactionListResponseDTO responseDTO) {
        validateGatewayConnectorResponses(
                responseDTO.getResponse(),
                responseDTO.getStatusModel(),
                responseDTO.getResponseMsg() == null ? SOMETHING_WENT_WRONG_MSG : responseDTO.getResponseMsg()
        );
        return responseDTO.getResponse();
    }

    public static ValidationResponseDTO getListOfTransactionsFromGatewayConnectorResponse(GatewayConnectorValidationResponseDTO responseDTO) {
        validateGatewayConnectorResponses(
                responseDTO.getResponse(),
                responseDTO.getStatusModel(),
                responseDTO.getResponseMsg() == null ? SOMETHING_WENT_WRONG_MSG : responseDTO.getResponseMsg()
        );
        return responseDTO.getResponse();
    }

    public static Pageable getPageable(String pageString, String sizeString) {
        int page = 1;
        int size = 10;
        try {
            page = Integer.parseInt(pageString);
            size = Integer.parseInt(sizeString);

        } catch (NumberFormatException e) {
            throw new InvalidFieldException("Numeric value expected for pagination details");
        }

        if(page < 0) throw new InvalidFieldException("Page number must be positive number");
        if(size <= 0 || size > 500) throw new InvalidFieldException("Page size must be greater than 0 and less than 500 in query");
        return Pageable.ofSize(size).withPage(page - 1);
    }

    private static void validateGatewayConnectorResponses(Object response, StatusModel statusModel, String responseMsg) {
        if(response == null && statusModel != null) {
            throw new ApiException(statusModel.getStatusCode(),responseMsg, statusModel.getStatusMsg());
        }
        else if(response == null) {
            throw new ApiException(503, SOMETHING_WENT_WRONG_MSG, INTERNAL_SERVER_ERROR);
        }
    }

}
