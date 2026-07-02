package rkoji.moaju.global.mail;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

	private final JavaMailSender mailSender;

	public void sendAlertMail(
		String to,
		String accountNickname,
		BigDecimal currentRate,
		BigDecimal targetRate,
		boolean achieved
	) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(to);
		message.setSubject("[모아주] 목표 수익률 달성 알람");
		message.setText(
			accountNickname + " 계좌의 수익률이 " + (achieved ? "목표를 달성했습니다." : "목표에 근접했습니다.") + "\n\n" +
				"현재 수익률: " + currentRate.setScale(2, RoundingMode.HALF_UP) + "%\n" +
				"목표 수익률: " + targetRate.setScale(2, RoundingMode.HALF_UP) + "%"
		);
		mailSender.send(message);
	}
}
