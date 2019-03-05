package chain.fxgj.server.payroll.mongodb.model;

import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author chain
 * create by chain on 2018/10/31 2:20 PM
 **/
@Setter
@Getter
@Document
public class Business {
    @Id
    private ObjectId id;
    /**
     * 业务系统 标识
     */
    @Indexed
    private String sysId;
    /**
     * 系统别名
     */
    private String sysName;
    /**
     * 系统回调地址
     */
    private String loginUrl;
    /**
     * 系统图标
     */
    private String icon;
    /**
     * 超时时间
     */
    private Integer timeOutMinute;
    /**
     * 创建时间
     */
    private LocalDateTime crtDateTime;
    /**
     * 修改时间
     */
    private LocalDateTime updDateTime;

    @Override
    public String toString() {
        return "Business{" +
                "id=" + id +
                ", sysId='" + sysId + '\'' +
                ", sysName='" + sysName + '\'' +
                ", loginUrl='" + loginUrl + '\'' +
                ", icon='" + icon + '\'' +
                ", timeOutMinute=" + timeOutMinute +
                ", crtDateTime=" + crtDateTime +
                ", updDateTime=" + updDateTime +
                '}';
    }
}
