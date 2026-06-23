package rkoji.moaju.account.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAccountRequest(

	@NotBlank(message = "증권사명을 입력해 주세요")
	@Size(max = 30, message = "증권사명은 30자 이하여야 합니다.")
	String brokerName,

	@NotBlank(message = "계좌 별칭을 입력해 주세요")
	@Size(max = 30, message = "계좌 별칭은 30자 이하여야 합니다.")
	String nickname
) {
}
