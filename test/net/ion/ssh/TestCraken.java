package net.ion.ssh;

import net.ion.craken.node.ReadSession;
import net.ion.craken.node.TransactionJob;
import net.ion.craken.node.WriteSession;
import net.ion.craken.node.crud.Craken;
import net.ion.craken.node.crud.tree.impl.PropertyValue;
import net.ion.framework.util.Debug;
import junit.framework.TestCase;

public class TestCraken extends TestCase{

	public void testBigStringNumberValue() throws Exception {
		
		Craken craken = Craken.inmemoryCreateWithTest();
		
		ReadSession rSession = craken.login("test");
		
		rSession.tran( new TransactionJob<Void>() {

			@Override
			public Void handle(WriteSession wsession) throws Exception {
				String bigNumber = "123123123123123123123123123123";
				wsession.pathBy("/test/").property("test" , PropertyValue.createPrimitive(bigNumber));
				return null;
			}
		});
	}
	
	public void testPropertyValueType() {
		PropertyValue value = PropertyValue.createPrimitive("123123123123123123123123123123");
		Debug.line(value.type());
	}
}
