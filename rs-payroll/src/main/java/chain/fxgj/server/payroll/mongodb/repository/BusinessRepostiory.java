package chain.fxgj.server.payroll.mongodb.repository;

import chain.fxgj.server.payroll.mongodb.model.Business;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author chain
 * create by chain on 2018/10/31 12:23 PM
 **/
public interface BusinessRepostiory extends MongoRepository<Business, ObjectId> {
    Optional<Business> findFirstBySysId(String sysId);

    List<Business> findAllByIdIsIn(Set<String> ids);
}
