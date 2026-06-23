package rkoji.moaju.account.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import rkoji.moaju.account.entity.BrokerageAccount;

public interface BrokerageAccountRepository extends JpaRepository<BrokerageAccount, Long> {

	List<BrokerageAccount> findAllByUserId(Long userId);

	Optional<BrokerageAccount> findByIdAndUserId(Long id, Long userId);
}
