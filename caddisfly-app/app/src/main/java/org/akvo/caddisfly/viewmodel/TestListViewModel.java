/*
 * Copyright 2017, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.akvo.caddisfly.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;

import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.model.TestType;
import org.akvo.caddisfly.repository.TestConfigRepository;

import java.util.List;

public class TestListViewModel extends AndroidViewModel {

    TestConfigRepository testConfigRepository;

    public TestListViewModel(@NonNull Application application) {
        super(application);
        testConfigRepository = new TestConfigRepository();
    }

    public List<TestInfo> getTests(TestType testType) {
        return testConfigRepository.getTests(testType);
    }

    public TestInfo getTestInfo(String uuid) {
        return testConfigRepository.getTestInfo(uuid);
    }

    public TestInfo getTestInfoByMd610Id(String md610Id) {
        return testConfigRepository.getTestInfoByMd610Id(md610Id);
    }

    public void clearTests() {
        testConfigRepository.clear();
    }
}
