package cn.gson.oasys.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * 项目标准化流程结果
 */
@Entity
@Table(name = "project_process")
@Data
public class ProjectProcess {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name", columnDefinition = "项目名称")
    private String name;
    @Column(name = "create_user", columnDefinition = "发起人")
    private String createUser;
    @Column(name = "create_user_id", columnDefinition = "发起人id")
    private String createUserId;
    @Column(name = "Stats", columnDefinition = "状态")
    private Stats stats;

    //外出拜访数据
    @Column(name = "business_presentation", columnDefinition = "外出拜访详情")
    private String businessPresentation;
    @Column(name = "issue", columnDefinition = "客户痛点")
    private String issue;
    @Column(name = "budget", columnDefinition = "预算")
    private String budget;
    @Column(name = "business_travel_file", columnDefinition = "业务拓展附件")
    private String businessTravelFile;

    //方案跟进数据
    @Column(name = "output_and_follow_up", columnDefinition = "方案输出/跟进是否通过")
    private boolean pass;

    @Column(name = "approved", columnDefinition = "是否立项")
    private boolean approved;

    @Column(name = "bid_preparation_and_bidding", columnDefinition = "招投标/标书制作")
    private Long bidPreparationAndBidding;
    @Column(name = "won_bid", columnDefinition = "中标后")
    private Long wonBid;
    @Column(name = "implementation", columnDefinition = "项目实施")
    private Long implementation;
    @Column(name = "system_development", columnDefinition = "系统研发")
    private Long systemDevelopment;
    @Column(name = "project_acceptance", columnDefinition = "项目验收")
    private Long projectAcceptance;
    @Column(name = "need_development", columnDefinition = "是否需要软件开发")
    private boolean needDevelopment;
    @Column(name = "done_file", columnDefinition = "归档资料")
    private String doneFile;
    @Column(name = "create_time", columnDefinition = "创建时间")
    private Date createTime;
    @Column(name = "end_time", columnDefinition = "结束时间")
    private Date endTime;
    @Column(name = "now_head", columnDefinition = "当前状态负责人")
    private String nowHead;

    public enum Stats {
        BUSINESS_TRAVEL("出差/外出访问", 1),
        SOLUTION_OUTPUT("方案输出", 2),
        FOLLOW_UP("方案跟进", 3),
        BID_PREPARATION("标书制作", 4),
        BIDDING("招投标", 5),
        WON_BID("已中标", 6),
        IMPLEMENTATION("实施", 7),
        SYSTEM_DEVELOPMENT("系统研发", 8),
        APPROVED("立项审核", 9),
        PROJECT_ACCEPTANCE("项目验收", 10),
        DONE("归档", 11);

        private String text;
        private int index;

        Stats(String text, int index) {
            this.text = text;
            this.index = index;
        }

        public static ProjectProcess.Stats getNextStats(Stats stats) {
            for (ProjectProcess.Stats value : ProjectProcess.Stats.values()) {
                if (value.getIndex() == (stats.getIndex() + 1)) {
                    return value;
                }
            }
            return null;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }
    }
}
