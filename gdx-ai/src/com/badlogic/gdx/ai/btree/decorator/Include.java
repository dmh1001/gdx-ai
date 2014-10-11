/*******************************************************************************
 * Copyright 2014 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.ai.btree.decorator;

import com.badlogic.gdx.ai.btree.BehaviorTreeLibraryManager;
import com.badlogic.gdx.ai.btree.Decorator;
import com.badlogic.gdx.ai.btree.Metadata;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.TaskCloneException;

/** An {@code Include} decorator grafts a subtree. When the subtree is grafted depends on the value of the {@link lazy} parameter:
 * at clone-time if is {@code false}, at run-time if is {@code true}.
 * 
 * @param <E> type of the blackboard object that tasks use to read or modify game state
 * 
 * @author davebaol
 * @author implicit-invocation */
public class Include<E> extends Decorator<E> {

	/** The task metadata specifying static information used by parsers and tools. */
	public static final Metadata METADATA = new Metadata("subtree", "lazy");

	/** The path of the subtree we're referencing to. */
	public String subtree;

	/** Whether the subtree should be included at clone-time (true) or at run-time (false, the default). */
	public boolean lazy;

	/** Creates a non-lazy {@code Include} decorator without specifying the subtree. */
	public Include () {
	}

	/** Creates a non-lazy {@code Include} decorator for the specified subtree.
	 * @param subtree the subtree reference, usually a path */
	public Include (String subtree) {
		this.subtree = subtree;
	}

	/** Creates an eager or lazy {@code Include} decorator for the specified subtree.
	 * @param subtree the subtree reference, usually a path
	 * @param lazy whether inclusion should happen at clone-time (false) or ar run-time (true) */
	public Include (String subtree, boolean lazy) {
		this.subtree = subtree;
		this.lazy = lazy;
	}

	/** The first call of this method lazily sets its child to the referenced subtree created through the
	 * {@link BehaviorTreeLibraryManager}, then invoke the same method of the superclass. Subsequent calls invoke the same method
	 * of the superclass directly since the child has already been set. A {@link UnsupportedOperationException} is thrown if this
	 * {@code Include} is eager.
	 * 
	 * @param control the parent task
	 * @throws UnsupportedOperationException if this {@code Include} is eager */
	@Override
	public void setControl (Task<E> control) {
		if (!lazy)
			throw new UnsupportedOperationException("A non-lazy " + Include.class.getSimpleName() + " isn't meant to be run!");

		if (child == null) {
			// Lazy include is grafted at run-time
			child = createSubtreeRootTask();
		}

		super.setControl(control);
	}

	/** Returns a clone of the referenced subtree if this {@code Import} is eager; otherwise returns a clone of itself. */
	public Task<E> cloneTask () {
		if (lazy) return super.cloneTask();

		// Non lazy include is grafted at clone-time
		return createSubtreeRootTask();
	}

	/** Copies this {@code Include} to the given task. A {@link TaskCloneException} is thrown if this {@code Include} is eager.
	 * @param task the task to be filled
	 * @return the given task for chaining
	 * @throws TaskCloneException if this {@code Include} is eager. */
	@Override
	protected Task<E> copyTo (Task<E> task) {
		if (!lazy) throw new TaskCloneException("A non-lazy " + getClass().getSimpleName() + " should never be copied.");

		Include<E> include = (Include<E>)task;
		include.subtree = subtree;
		include.lazy = lazy;

		return task;
	}

	private Task<E> createSubtreeRootTask () {
		return BehaviorTreeLibraryManager.getInstance().createRootTask(subtree);
	}
}
