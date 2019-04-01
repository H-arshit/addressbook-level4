package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_REMARK;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import seedu.address.commons.core.Messages;
import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.CollectionUtil;
import seedu.address.logic.CommandHistory;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.tag.Tag;
import seedu.address.model.person.Remark;

/**
 * Edits the remark for a person specified in the INDEX.
 */
public class RemarkCommand extends Command {

    public static final String COMMAND_WORD = "remark";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Edits the remark of the person identified "
            + "by the index number used in the displayed person list. "
            + "Existing values will be overwritten by the input values.\n"
            + "Parameters: INDEX (must be a positive integer) "
            + "[" + PREFIX_REMARK + "TEXT] "
            + "Example: " + COMMAND_WORD + " 1 "
            + PREFIX_REMARK + "Likes to drink coffee.";

    public static final String MESSAGE_REMARK_UPDATED_SUCCESS = "Remark updated: %1$s";
    public static final String MESSAGE_REMARK_NOT_ADDED = "Remark not added, remark requires text input.";
    public static final String MESSAGE_DUPLICATE_PERSON = "This person already exists in the address book.";

    private final Index index;
    private final RemarkPersonDescriptor remarkPersonDescriptor;

    /**
     * @param index of the person in the filtered person list to edit
     * @param remarkPersonDescriptor details to edit the person with
     */
    public RemarkCommand(Index index, RemarkPersonDescriptor remarkPersonDescriptor) {
        requireNonNull(index);
        requireNonNull(remarkPersonDescriptor);

        this.index = index;
        this.remarkPersonDescriptor = new RemarkPersonDescriptor(remarkPersonDescriptor);
    }

    @Override
    public CommandResult execute(Model model, CommandHistory history) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();

        if (index.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person personToRemark = lastShownList.get(index.getZeroBased());
        Person remarkedPerson = createRemarkedPerson(personToRemark, remarkPersonDescriptor);

        if (!personToRemark.isSamePerson(remarkedPerson) && model.hasPerson(remarkedPerson)) {
            throw new CommandException(MESSAGE_DUPLICATE_PERSON);
        }

        model.setPerson(personToRemark, remarkedPerson);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        model.commitAddressBook();
        return new CommandResult(String.format(MESSAGE_REMARK_UPDATED_SUCCESS, remarkedPerson));
    }

    /**
     * Creates and returns a {@code Person} with the remark of {@code personToRemark}
     * edited with {@code remarkPersonDescriptor}.
     */
    private static Person createRemarkedPerson(Person personToRemark, RemarkPersonDescriptor remarkPersonDescriptor) {
        assert personToRemark != null;

        Name updatedName = remarkPersonDescriptor.getName().orElse(personToRemark.getName());
        Phone updatedPhone = remarkPersonDescriptor.getPhone().orElse(personToRemark.getPhone());
        Email updatedEmail = remarkPersonDescriptor.getEmail().orElse(personToRemark.getEmail());
        Address updatedAddress = remarkPersonDescriptor.getAddress().orElse(personToRemark.getAddress());
        Set<Tag> updatedTags = remarkPersonDescriptor.getTags().orElse(personToRemark.getTags());
        Remark updatedRemark = remarkPersonDescriptor.getRemark().orElse(personToRemark.getRemark());

        return new Person(updatedName, updatedPhone, updatedEmail, updatedAddress, updatedTags, updatedRemark);
    }

    @Override
    public boolean equals(Object other) {
        // short circuit if same object
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof RemarkCommand)) {
            return false;
        }

        // state check
        RemarkCommand e = (RemarkCommand) other;
        return index.equals(e.index)
                && remarkPersonDescriptor.equals(e.remarkPersonDescriptor);
    }

    /**
     * Stores the details to edit the person with. Each non-empty field value will replace the
     * corresponding field value of the person.
     */
    public static class RemarkPersonDescriptor {
        private Name name;
        private Phone phone;
        private Email email;
        private Address address;
        private Set<Tag> tags;
        private Remark remark;

        public RemarkPersonDescriptor() {}

        /**
         * Copy constructor.
         * A defensive copy of {@code tags} is used internally.
         */
        public RemarkPersonDescriptor(RemarkPersonDescriptor toCopy) {
            setName(toCopy.name);
            setPhone(toCopy.phone);
            setEmail(toCopy.email);
            setAddress(toCopy.address);
            setTags(toCopy.tags);
            setRemark(toCopy.remark);
        }

        /**
         * Returns true if at least one field is edited.
         */
        public boolean isRemarkEdited() {
            return CollectionUtil.isAnyNonNull(remark);
        }

        public void setName(Name name) {
            this.name = name;
        }

        public Optional<Name> getName() {
            return Optional.ofNullable(name);
        }

        public void setPhone(Phone phone) {
            this.phone = phone;
        }

        public Optional<Phone> getPhone() {
            return Optional.ofNullable(phone);
        }

        public void setEmail(Email email) {
            this.email = email;
        }

        public Optional<Email> getEmail() {
            return Optional.ofNullable(email);
        }

        public void setAddress(Address address) {
            this.address = address;
        }

        public Optional<Address> getAddress() {
            return Optional.ofNullable(address);
        }

        /**
         * Sets {@code tags} to this object's {@code tags}.
         * A defensive copy of {@code tags} is used internally.
         */
        public void setTags(Set<Tag> tags) {
            this.tags = (tags != null) ? new HashSet<>(tags) : null;
        }

        /**
         * Returns an unmodifiable tag set, which throws {@code UnsupportedOperationException}
         * if modification is attempted.
         * Returns {@code Optional#empty()} if {@code tags} is null.
         */
        public Optional<Set<Tag>> getTags() {
            return (tags != null) ? Optional.of(Collections.unmodifiableSet(tags)) : Optional.empty();
        }

        public void setRemark(Remark remark) {
            this.remark = remark;
        }

        public Optional<Remark> getRemark() {
            return Optional.ofNullable(remark);
        }

        @Override
        public boolean equals(Object other) {
            // short circuit if same object
            if (other == this) {
                return true;
            }

            // instanceof handles nulls
            if (!(other instanceof RemarkPersonDescriptor)) {
                return false;
            }

            // state check
            RemarkPersonDescriptor e = (RemarkPersonDescriptor) other;

            return getName().equals(e.getName())
                    && getPhone().equals(e.getPhone())
                    && getEmail().equals(e.getEmail())
                    && getAddress().equals(e.getAddress())
                    && getTags().equals(e.getTags())
                    && getRemark().equals(e.getRemark());
        }
    }
}
