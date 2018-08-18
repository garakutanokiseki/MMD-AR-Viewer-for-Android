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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import jp.nyatla.nymmd.IMmdDataIo;
import jp.nyatla.nymmd.IMmdPmdRender;
import jp.nyatla.nymmd.MmdException;
import jp.nyatla.nymmd.MmdPmdModel;
import jp.nyatla.nymmd.types.*;

import java.util.*;

import javax.microedition.khronos.opengles.GL11;

import android.graphics.Bitmap;
import android.opengl.GLUtils;
import android.os.SystemClock;
import jp.gr.java_conf.ka_ka_xyz.util.Log;

class GLTextureData {
	public int gl_texture_id;

	public String file_name;
}

class GLTextureList {
	private final ArrayList<GLTextureData> m_pTextureList = new ArrayList<GLTextureData>();

	public void reset(GL11 _gl) {
		GL11 gl = (GL11) _gl;
		for (int i = 0; i < m_pTextureList.size(); i++) {
			final int[] ids = new int[1];
			ids[0] = this.m_pTextureList.get(i).gl_texture_id;
			gl.glDeleteTextures(1, ids, 0);
		}
		this.m_pTextureList.clear();
		return;
	}

	private GLTextureData createTexture(GL11 _gl, String szFileName, Bitmap img)
			throws MmdException {
		GL11 gl = (GL11) _gl;
		IntBuffer texid = IntBuffer.allocate(1);
		if (img == null) {
			return null;
		}
		// 第一引数は画像枚数
		gl.glGenTextures(1, texid);
		gl.glBindTexture(GL11.GL_TEXTURE_2D, texid.get(0));
		gl.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);

		gl.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER,
				GL11.GL_LINEAR);
		gl.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER,
				GL11.GL_LINEAR);
		gl.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S,
				GL11.GL_REPEAT);
		gl.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T,
				GL11.GL_REPEAT);
		// 転写
		gl.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE,
				GL11.GL_REPLACE);
		// int[] rgb_array = new int[img.getWidth() * img.getHeight()];
		// img.getPixels(rgb_array, 0, img.getWidth(), img.getHeight(), 0, 0,
		// img.getWidth());
		// _gl.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, img.getWidth(),
		// img.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE,
		// IntBuffer.wrap(rgb_array));
		GLUtils.texImage2D(GL11.GL_TEXTURE_2D, 0, img, 0);

		gl.glEnable(GL11.GL_TEXTURE_2D);
		gl.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
		// FloatBuffer prio = FloatBuffer.allocate(1);
		// prio.put(0, 1.0f);
		// _gl.glPrioritizeTextures(1, texid, prio);

		GLTextureData ret = new GLTextureData();
		ret.file_name = szFileName;
		ret.gl_texture_id = texid.get(0);
		return ret;
	}

	public GLTextureData getTexture(GL11 _gl, String i_filename, IMmdDataIo i_io)
			throws MmdException {
		GLTextureData ret;

		final int len = this.m_pTextureList.size();
		for (int i = 0; i < len; i++) {
			ret = this.m_pTextureList.get(i);
			if (ret.file_name.equalsIgnoreCase(i_filename)) {
				// 読み込み済みのテクスチャを発見
				return ret;
			}
		}

		// なければファイルを読み込んでテクスチャ作成
		ret = createTexture(_gl, i_filename, i_io.getTextureBitmap(i_filename));
		if (ret != null) {
			this.m_pTextureList.add(ret);
			return ret;
		}

		return null;// テクスチャ読み込みか作成失敗

	}
}

class GLMaterial {
	public final float[] color = new float[12];// Diffuse,Specular,Ambientの順
	public float fShininess;
	public ShortBuffer indices;
	public int ulNumIndices;
	public GLTextureData texture;
	public int unknown;
}

public class MmdPmdRenderGL11 implements IMmdPmdRender {
	private static final String TAG = "MmdPmdRenderGL11";
	
	protected static MmdPmdModel _ref_pmd;

	protected static GLMaterial[] _gl_materials;
	// レンダリング時の計算用
	protected static MmdVector3[] _position_array;
	protected static MmdVector3[] _normal_array;

	// private GL11 _gl;

	protected static GLTextureList _textures;

	public MmdPmdRenderGL11() {
		_textures = new GLTextureList();
		return;
	}

	public void dispose(GL11 gl) {
		_textures.reset(gl);
		
		gl.glDisableClientState(GL11.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL11.GL_NORMAL_ARRAY);
		gl.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
	}
	
	/**
	 * レンダリング対象のPMDを設定する。
	 * 
	 * @param i_pmd
	 */
	public void setPmd(GL11 _gl, MmdPmdModel i_pmd, IMmdDataIo i_io)
			throws MmdException {
		// テクスチャリストのリセット
		_textures.reset(_gl);
		// PMD内部を参照（サボってるので、i_pmdを消さないこと）
		_ref_pmd = i_pmd;
		// 内部用
		final int number_of_vertex = i_pmd.getNumberOfVertex();
		_position_array = MmdVector3.createArray(number_of_vertex);
		_normal_array = MmdVector3.createArray(number_of_vertex);

		// Material配列の作成
		PmdMaterial[] m = i_pmd.getMaterials();// this._ref_materials;
		Vector<GLMaterial> gl_materials = new Vector<GLMaterial>();
		for (int i = 0; i < m.length; i++) {
			final GLMaterial new_material = new GLMaterial();
			new_material.unknown = m[i].unknown;
			// D,A,S[rgba]
			m[i].col4Diffuse.getValue(new_material.color, 0);
			m[i].col4Ambient.getValue(new_material.color, 4);
			m[i].col4Specular.getValue(new_material.color, 8);
			new_material.fShininess = m[i].fShininess;
			if (m[i].texture_name != null) {
				new_material.texture = _textures.getTexture(_gl,
						m[i].texture_name, i_io);
			} else {
				new_material.texture = null;
			}
			new_material.indices = ShortBuffer.wrap(m[i].indices);
			new_material.ulNumIndices = m[i].indices.length;
			gl_materials.add(new_material);
		}
		_gl_materials = gl_materials.toArray(new GLMaterial[gl_materials
				.size()]);
		return;
	}

	private final MmdMatrix __tmp_matrix = new MmdMatrix();

	/**
	 * i_skinning_matでPMDを更新した頂点データを取得する。
	 * 
	 * @param i_skinning_mat
	 * @param i_pos_array
	 * @param i_normal_array
	 * @param i_skin_info
	 */
	public void updateSkinning(MmdMatrix[] i_skinning_mat) {
		int number_of_vertex = _ref_pmd.getNumberOfVertex();
		MmdVector3[] org_pos_array = _ref_pmd.getPositionArray();
		MmdVector3[] org_normal_array = _ref_pmd.getNormatArray();
		PmdSkinInfo[] org_skin_info = _ref_pmd.getSkinInfoArray();

		// 頂点スキニング
		final MmdMatrix matTemp = this.__tmp_matrix;
		for (int i = 0; i < number_of_vertex; i++) {
			if (org_skin_info[i].fWeight == 0.0f) {
				final MmdMatrix mat = i_skinning_mat[org_skin_info[i].unBoneNo[1]];
				_position_array[i].Vector3Transform(org_pos_array[i], mat);
				_normal_array[i].Vector3Rotate(org_normal_array[i], mat);
			} else if (org_skin_info[i].fWeight >= 0.9999f) {
				final MmdMatrix mat = i_skinning_mat[org_skin_info[i].unBoneNo[0]];
				_position_array[i].Vector3Transform(org_pos_array[i], mat);
				_normal_array[i].Vector3Rotate(org_normal_array[i], mat);
			} else {
				final MmdMatrix mat0 = i_skinning_mat[org_skin_info[i].unBoneNo[0]];
				final MmdMatrix mat1 = i_skinning_mat[org_skin_info[i].unBoneNo[1]];

				matTemp.MatrixLerp(mat0, mat1, org_skin_info[i].fWeight);

				_position_array[i].Vector3Transform(org_pos_array[i],
						matTemp);
				_normal_array[i].Vector3Rotate(org_normal_array[i],
						matTemp);
			}
		}
		return;
	}
	
	public void render(GL11 gl) {
		
		long init = SystemClock.currentThreadTimeMillis();
		final MmdTexUV[] texture_uv = _ref_pmd.getUvArray();
		final int number_of_vertex = _ref_pmd.getNumberOfVertex();

		gl.glEnableClientState(GL11.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL11.GL_NORMAL_ARRAY);
		gl.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
		// とりあえずbufferに変換しよう
		final ByteBuffer pos_buf = ByteBuffer
				.allocateDirect(_position_array.length * 3 * 4);
		pos_buf.order(ByteOrder.LITTLE_ENDIAN);
		for (int i = 0; i < number_of_vertex; i++) {
			pos_buf.putFloat(_position_array[i].x);
			pos_buf.putFloat(_position_array[i].y);
			pos_buf.putFloat(_position_array[i].z);
		}
		final ByteBuffer nom_array = ByteBuffer
				.allocateDirect(_position_array.length * 3 * 4);
		nom_array.order(ByteOrder.LITTLE_ENDIAN);
		for (int i = 0; i < number_of_vertex; i++) {
			nom_array.putFloat(_normal_array[i].x);
			nom_array.putFloat(_normal_array[i].y);
			nom_array.putFloat(_normal_array[i].z);
		}
		final ByteBuffer tex_array = ByteBuffer
				.allocateDirect(texture_uv.length * 2 * 4);
		tex_array.order(ByteOrder.LITTLE_ENDIAN);
		for (int i = 0; i < number_of_vertex; i++) {
			tex_array.putFloat(texture_uv[i].u);
			tex_array.putFloat(texture_uv[i].v);
		}
		pos_buf.position(0);
		nom_array.position(0);
		tex_array.position(0);
		// とりあえず転写用
		// 頂点座標、法線、テクスチャ座標の各配列をセット
		//noVBO
		gl.glVertexPointer(3, GL11.GL_FLOAT, 0, pos_buf);
		gl.glNormalPointer(GL11.GL_FLOAT, 0, nom_array);
		gl.glTexCoordPointer(2, GL11.GL_FLOAT, 0, tex_array);
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

		gl.glDisableClientState(GL11.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL11.GL_NORMAL_ARRAY);
		gl.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);

		long aft = SystemClock.currentThreadTimeMillis();
		Log.d(TAG, "draw: " + (aft - init) + " ms");
	}
}

class FlushedInputStream extends FilterInputStream {
	public FlushedInputStream(InputStream inputStream) {
		super(inputStream);
	}

	@Override
	public long skip(long n) throws IOException {
		long totalBytesSkipped = 0L;
		while (totalBytesSkipped < n) {
			long bytesSkipped = in.skip(n - totalBytesSkipped);
			if (bytesSkipped == 0L) {
				int bytes = read();
				if (bytes < 0) {
					break; // we reached EOF
				} else {
					bytesSkipped = 1; // we read one byte
				}
			}
			totalBytesSkipped += bytesSkipped;
		}
		return totalBytesSkipped;
	}
}
