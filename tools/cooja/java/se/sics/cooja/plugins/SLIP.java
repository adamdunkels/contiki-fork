/*
 * Copyright (c) 2009, Swedish Institute of Computer Science.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the Institute nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE INSTITUTE AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE INSTITUTE OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 */

package se.sics.cooja.plugins;

import java.util.ArrayList;
import java.util.Vector;

import org.apache.log4j.Logger;

public class SLIP {
	private static Logger logger = Logger.getLogger(SLIP.class);

	private final static byte SLIP_END = (byte) 0300;
	private final static byte SLIP_ESC = (byte) 0333;
	private final static byte SLIP_ESC_END = (byte) 0334;
	private final static byte SLIP_ESC_ESC = (byte) 0335;

	/**
	 * Wraps packet as SLIP.
	 * 
	 * @param packet Packet data
	 */
	public static byte[] asSlip(byte[] packet) {
		ArrayList<Byte> bytes = new ArrayList<Byte>();

		bytes.add(SLIP_END);

		for (byte b : packet) {
			if (b == SLIP_END) {
				bytes.add(SLIP_ESC);
				bytes.add(SLIP_ESC_END);
			} else if (b == SLIP_ESC) {
				bytes.add(SLIP_ESC);
				bytes.add(SLIP_ESC_ESC);
			} else {
				bytes.add(b);
			}
		}

		bytes.add(SLIP_END);

		byte[] arr = new byte[bytes.size()];
		for (int i = 0; i < bytes.size(); i++) {
			arr[i] = (bytes.get(i)).byteValue();
		}
		return arr;
	}

	public static boolean hasSlipEnd(Vector<Byte> slipped) {
		boolean wasStateEsc = false;
		for(byte b: slipped) {
			if (wasStateEsc) {
				wasStateEsc = false;
			} else if (b == SLIP_ESC) {
				wasStateEsc = true;
			} else if (b == SLIP_END) {
				return true;
			}
		}
		return false;
	}

	public static byte[] unSlip(Vector<Byte> slipped) {
		ArrayList<Byte> unslipped = new ArrayList<Byte>();

		boolean wasStateEsc = false;
		while(slipped.size() > 0) {
			byte b = slipped.remove(0);
			if (wasStateEsc) {
				wasStateEsc = false;
				if (b == SLIP_ESC_END) {
					unslipped.add(SLIP_END);
				} else if (b == SLIP_ESC_ESC) {
					unslipped.add(SLIP_ESC);
				}
			} else if (b == SLIP_ESC) {
				wasStateEsc = true;
			} else if (b == SLIP_END) {
				break;
			} else {
				unslipped.add(b);
			}
		}

		byte[] arr = new byte[unslipped.size()];
		for (int i = 0; i < unslipped.size(); i++) {
			arr[i] = (unslipped.get(i)).byteValue();
		}
		return arr;
	}
}
