package io.github.classops.urouter;

import io.github.classops.urouter.utils.Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UtilsTest {

    @Test
    public void test() {
        Assertions.assertEquals(Utils.getValidTypeName("feat:user"), "feat_user");
        Assertions.assertEquals(Utils.getValidTypeName("feat-user"), "feat_user");
        Assertions.assertEquals(Utils.getValidTypeName("0feat-user"), "0feat_user");
    }

}
