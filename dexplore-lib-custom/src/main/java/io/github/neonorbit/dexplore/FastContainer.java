/*
 * Copyright (C) 2022 NeonOrbit
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.neonorbit.dexplore;

import io.github.neonorbit.dexplore.iface.Internal;
import com.android.tools.smali.dexlib2.Opcodes;
import com.android.tools.smali.dexlib2.dexbacked.DexBackedDexFile;
import com.android.tools.smali.dexlib2.dexbacked.ZipDexContainer;
import com.android.tools.smali.dexlib2.util.DexUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lanchon.multidexlib2.BasicDexFileNamer;
import lanchon.multidexlib2.ZipFileDexContainer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Internal
final class FastContainer extends ZipFileDexContainer {
  private final boolean isApkFile;
  private final boolean rootDexOnly;
  private FastContainer(@Nonnull File zipFilePath,
                        @Nullable Opcodes opcodes,
                        boolean isApkFile, boolean rootDexOnly) throws IOException {
    super(zipFilePath, new BasicDexFileNamer(), opcodes);
    this.isApkFile = isApkFile;
    this.rootDexOnly = rootDexOnly;
  }

  @Nullable
  public static FastContainer load(@Nonnull File zipFilePath,
                                   @Nullable Opcodes opcodes,
                                   boolean rootDexOnly) {
    try (ZipFile zip = new ZipFile(zipFilePath)) {
      boolean isApkFile = rootDexOnly || zip.getEntry("AndroidManifest.xml") != null;
      return new FastContainer(zipFilePath, opcodes, isApkFile, rootDexOnly);
    } catch (IOException ignore) {
      return null;
    }
  }

  private boolean isDexSuper(@Nonnull ZipFile zipFile,
                             @Nonnull ZipEntry zipEntry) throws IOException{
      try (InputStream inputStream = new BufferedInputStream(zipFile.getInputStream(zipEntry))) {
          DexUtil.verifyDexHeader(inputStream);
      } catch (DexBackedDexFile.NotADexFile ex) {
          return false;
      } catch (DexUtil.InvalidFile ex) {
          return false;
      } catch (DexUtil.UnsupportedFile ex) {
          return false;
      }
      return true;
  }

  private boolean isDex(@Nonnull ZipFile zipFile,
                        @Nonnull ZipEntry zipEntry) throws IOException {
    String name = zipEntry.getName();
    boolean filter = rootDexOnly ? name.startsWith("classes") && name.endsWith(".dex") : !(
            isApkFile && (name.startsWith("r/") || name.startsWith("res/") || name.startsWith("lib/"))
    );
    return filter && isDexSuper(zipFile, zipEntry);
  }
}
