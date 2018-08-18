/* 
 * PROJECT: MMD for Java
 * --------------------------------------------------------------------------------
 * This work is based on the ARTK_MMD v0.1 
 *   PY
 * http://ppyy.hp.infoseek.co.jp/
 * py1024<at>gmail.com
 * http://www.nicovideo.jp/watch/sm7398691
 *
 * The MMD for Java is Java version MMD class library.
 * Copyright (C)2009 nyatla
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this framework; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * For further information please contact.
 *	http://nyatla.jp/
 *	<airmail(at)ebony.plala.or.jp>
 * 
 */
package jp.gr.java_conf.ka_ka_xyz.nyatla.nymmd;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import jp.nyatla.nymmd.IMmdDataIo;
import jp.nyatla.nymmd.MmdException;
import jp.nyatla.nymmd.MmdPmdModel;
import jp.nyatla.nymmd.types.*;
import javax.microedition.khronos.opengles.GL11;

import android.os.SystemClock;
import jp.gr.java_conf.ka_ka_xyz.util.Log;

public class MmdPmdRenderGL11VBO extends MmdPmdRenderGL11 {

	
	private static final String TAG = "MmdPmdRenderGL11VBO";
	public MmdPmdRenderGL11VBO() {
		super();
		Log.d(TAG, "vbo enabled");
	}

	private static ByteBuffer pos_buf = null;
	
	/**
	 * レンダリング対象のPMDを設定する。
	 * 
	 * @param i_pmd
	 */
	public void setPmd(GL11 _gl, MmdPmdModel i_pmd, IMmdDataIo i_io)
			throws MmdException {

		super.setPmd(_gl, i_pmd, i_io);
		MmdTexUV[] texture_uv = _ref_pmd.getUvArray();
		pos_buf = ByteBuffer
		.allocateDirect(_position_array.length * 3 * 4
			+ _normal_array.length * 3 * 4
			+ texture_uv.length * 2 * 4)
		.order(order);
		
		_gl.glEnableClientState(GL11.GL_VERTEX_ARRAY);
		_gl.glEnableClientState(GL11.GL_NORMAL_ARRAY);
		_gl.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
		
		return;
	}
	
	private static final ByteOrder order = ByteOrder.nativeOrder();
	private static final int[] buffers = new int[1];
	private static final int BUFFER_ID = 1; 
	
	@Override
	public void render(GL11 gl) {
		long init = SystemClock.currentThreadTimeMillis();
		final MmdTexUV[] texture_uv = _ref_pmd.getUvArray();
		final int number_of_vertex = _ref_pmd.getNumberOfVertex();

		// とりあえずbufferに変換しよう
		//VBO
		pos_buf.clear();
		for (int i = 0; i < number_of_vertex; i++) {
			pos_buf.putFloat(_position_array[i].x);
			pos_buf.putFloat(_position_array[i].y);
			pos_buf.putFloat(_position_array[i].z);
			pos_buf.putFloat(_normal_array[i].x);
			pos_buf.putFloat(_normal_array[i].y);
			pos_buf.putFloat(_normal_array[i].z);
			pos_buf.putFloat(texture_uv[i].u);
			pos_buf.putFloat(texture_uv[i].v);
		}
		pos_buf.position(0);
		gl.glGenBuffers(1 ,buffers, 0);
		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, BUFFER_ID);
		gl.glBufferData(GL11.GL_ARRAY_BUFFER, pos_buf.capacity(), pos_buf, GL11.GL_STATIC_DRAW);
		gl.glVertexPointer(3, GL11.GL_FLOAT, 4*8, 0);
		gl.glNormalPointer(GL11.GL_FLOAT, 4*8, 4*3);
		gl.glTexCoordPointer(2, GL11.GL_FLOAT, 4*8, 4*6);
		//noVBO end
		
		int vertex_index = 0;
		for (int i = 0; i < _gl_materials.length; i++) {
			// マテリアル設定
			gl.glMaterialfv(GL11.GL_FRONT_AND_BACK, GL11.GL_DIFFUSE,
					_gl_materials[i].color, 0);
			gl.glMaterialfv(GL11.GL_FRONT_AND_BACK, GL11.GL_AMBIENT,
					_gl_materials[i].color, 4);
			gl.glMaterialfv(GL11.GL_FRONT_AND_BACK, GL11.GL_SPECULAR,
					_gl_materials[i].color, 8);
			gl.glMaterialf(GL11.GL_FRONT_AND_BACK, GL11.GL_SHININESS,
					_gl_materials[i].fShininess);

			// カリング判定：何となくうまくいったから
			if ((0x100 & _gl_materials[i].unknown) == 0x100) {
				gl.glDisable(GL11.GL_CULL_FACE);
			} else {
				gl.glEnable(GL11.GL_CULL_FACE);
			}

			if (_gl_materials[i].texture != null) {
				// テクスチャありならBindする
				gl.glEnable(GL11.GL_TEXTURE_2D);
				gl.glBindTexture(GL11.GL_TEXTURE_2D,
						_gl_materials[i].texture.gl_texture_id);
			} else {
				// テクスチャなし
				gl.glDisable(GL11.GL_TEXTURE_2D);
			}
			// 頂点インデックスを指定してポリゴン描画
			gl.glDrawElements(GL11.GL_TRIANGLES,
					_gl_materials[i].ulNumIndices, GL11.GL_UNSIGNED_SHORT,
					_gl_materials[i].indices);
			vertex_index += _gl_materials[i].ulNumIndices;
		}
		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
		long aft = SystemClock.currentThreadTimeMillis();
		Log.d(TAG, "draw: " + (aft - init) + " ms");
	}
}

