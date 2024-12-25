package cn.gson.oasys.vo;

import cn.gson.oasys.entity.File;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class FileListVo {
    private File nowFile;
    private List<File> file;
    private Set<String> tags;
}
