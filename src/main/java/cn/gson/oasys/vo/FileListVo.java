package cn.gson.oasys.vo;

import cn.gson.oasys.entity.File;
import lombok.Data;

import java.util.List;

@Data
public class FileListVo {
    private File nowFile;
    private List<File> file;
}
