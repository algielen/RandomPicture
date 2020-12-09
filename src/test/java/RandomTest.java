import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;


public class RandomTest {

	@Disabled
	@Test
	void testErasure() {
		List<Integer> integers = new ArrayList<>();

		List basicList = integers;
		basicList.add("haha");

		for (Object o : basicList) {
			System.out.println(o);
		}

		for (Integer integer : integers) {
			System.out.println(integer);
		}
	}
}
